package org.babi.backend.question.dao;

import org.babi.backend.category.dao.CategoryRepository;
import org.babi.backend.category.domain.Category;
import org.babi.backend.common.exception.ResourceNotFoundException;
import org.babi.backend.dao.AbstractDaoITTest;
import org.babi.backend.image.dao.ImageRepository;
import org.babi.backend.image.domain.Image;
import org.babi.backend.question.domain.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.r2dbc.core.DatabaseClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QuestionRepositoryImplITTest extends AbstractDaoITTest {

    private QuestionRepository questionRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    public QuestionRepositoryImplITTest(ImageRepository imageRepository, CategoryRepository categoryRepository, DatabaseClient databaseClient) {
        this.questionRepository = new QuestionRepositoryImpl(databaseClient);
        this.imageRepository = imageRepository;
        this.categoryRepository = categoryRepository;
        this.databaseClient = databaseClient;
    }

    @BeforeEach
    void restoreDatabase() {
        questionRepository.deleteAll().block();
        categoryRepository.deleteAll().block();
    }

    @Test
    void findAll_whenThereAreNoQuestions_thenExpectEmpty() {
        // given

        // when
        List<Question> result = questionRepository.findAll().collectList().block();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_whenThereIsQuestionWithProvidedId_thenShouldBeFound() {
        // given
        Image image = imageRepository.save(new Image(null, new byte[]{}, LocalDateTime.now())).block();
        Category category = categoryRepository.save(new Category(null, "category")).block();
        Question question = questionRepository.save(new Question(null, "text", image.getId(), image, Set.of(category.getId()), List.of(category), null, null, 0, 0)).block();

        // when
        Question result = questionRepository.findById(question.getId()).block();

        // then
        assertNotNull(result);
        assertEquals(question.getId(), result.getId());
    }

    @Test
    void findById_whenThereIsNoQuestionWithProvidedId_thenShouldThrowException() {
        // given

        // when
        assertThrows(ResourceNotFoundException.class, () -> questionRepository.findById(1L).block());

        // then
    }

    @Test
    void save_whenProvidedForeignEntityKeyDoesNotExist_thenExpectException() {
        // given

        // when
        assertThrows(IllegalArgumentException.class, () -> questionRepository.save(new Question(null, "text", null, null, null, null, null, null, 0, 0)).block());

        // then
    }

    @Test
    void save_whenValidQuestionProvided_thenExpectSaved() {
        // given
        final Image image = new Image(null, new byte[]{}, LocalDateTime.now());
        final Category category = new Category(null, "category");
        final Question question = new Question(null, "text", null, image, null, List.of(category), null, null, 0, 0);

        // when
        imageRepository.save(image)
                .flatMap(i -> {
                    Long imageId = i.getId();
                    image.setId(imageId);
                    question.setIconId(imageId);
                    return categoryRepository.save(category);
                })
                .flatMap(c -> {
                    Long categoryId = c.getId();
                    category.setId(categoryId);
                    question.setCategoriesId(Set.of(categoryId));
                    return questionRepository.save(question);
                })
                .block();

        Question result = questionRepository.findAll().blockFirst();


        // then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getIconId());
        Set<Long> categoriesId = result.getCategoriesId();
        assertNotNull(categoriesId);
        assertFalse(categoriesId.isEmpty());
        List<Category> categories = result.getCategories();
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
    }

    @Test
    void linkCategories_whenQuestionDoesNotExist_thenThrowException() {
        // given

        // when
        assertThrows(DataIntegrityViolationException.class, () -> questionRepository.linkCategories(1L, Set.of(1L, 2L, 3L)).block());

        // then
    }

    @Test
    void linkCategories_whenQuestionAndCategoryExist_thenLink() {
        // given
        final Image image = new Image(null, new byte[]{}, LocalDateTime.now());
        final Question question = new Question(null, "text", null, image, null, null, null, null, 0, 0);

        // when
        Question dbQuestion = imageRepository.save(image)
                .flatMap(i -> {
                    Long imageId = i.getId();
                    image.setId(imageId);
                    question.setIconId(imageId);
                    return questionRepository.save(question);
                }).block();
        Category category = categoryRepository.save(new Category(null, "category")).block();
        questionRepository.linkCategories(dbQuestion.getId(), Set.of(category.getId())).block();
        Question result = questionRepository.findAll().blockFirst();

        // then
        assertNotNull(result);
        Set<Long> resultCategoriesId = result.getCategoriesId();
        assertNotNull(resultCategoriesId);
        assertEquals(1, resultCategoriesId.size());
    }

    @Test
    void unlinkCategories_whenQuestionExistAndCategoryIsAlreadyLinked_thenUnlink() {
        // given
        final Image image = new Image(null, new byte[]{}, LocalDateTime.now());
        final Question question = new Question(null, "text", null, image, null, null, null, null, 0, 0);
        Question dbQuestion = imageRepository.save(image)
                .flatMap(i -> {
                    Long imageId = i.getId();
                    image.setId(imageId);
                    question.setIconId(imageId);
                    return questionRepository.save(question);
                }).block();
        Category category1 = categoryRepository.save(new Category(null, "category")).block();
        Category category2 = categoryRepository.save(new Category(null, "category")).block();
        questionRepository.linkCategories(dbQuestion.getId(), Set.of(category1.getId(), category2.getId())).block();

        // when
        questionRepository.unlinkCategories(dbQuestion.getId(), Set.of(category2.getId())).block();
        Question result = questionRepository.findAll().blockFirst();

        // then
        assertNotNull(result);
        Set<Long> resultCategoriesId = result.getCategoriesId();
        assertNotNull(resultCategoriesId);
        assertEquals(1, resultCategoriesId.size());
    }

    @Test
    void linkPreviousQuestions_whenBothQuestionsExist_thenLink() {
        // given
        Image image = imageRepository.save(new Image(null, new byte[]{}, LocalDateTime.now())).block();
        Category category = categoryRepository.save(new Category(null, "category")).block();
        final Question question1 = new Question(null, "text", image.getId(), image, Set.of(category.getId()), List.of(category), null, null, 0, 0);
        final Question question2 = new Question(null, "text", image.getId(), image, Set.of(category.getId()), List.of(category), null, null, 0, 0);
        Question dbQuestion1 = questionRepository.save(question1).block();
        Question dbQuestion2 = questionRepository.save(question2).block();
        questionRepository.linkCategories(dbQuestion1.getId(), Set.of(category.getId())).block();

        // when
        questionRepository.linkPreviousQuestions(dbQuestion1.getId(), Set.of(dbQuestion2.getId())).block();

        // then
        Question result = questionRepository.findById(dbQuestion1.getId()).block();
        assertNotNull(result);
        Set<Long> previousQuestionsId = result.getPreviousQuestionsId();
        assertNotNull(previousQuestionsId);
        assertEquals(1, previousQuestionsId.size());
    }

    @Test
    void unlinkPreviousQuestions_whenQuestionIsLinked_thenUnlink() {
        // given
        Image image = imageRepository.save(new Image(null, new byte[]{}, LocalDateTime.now())).block();
        Category category = categoryRepository.save(new Category(null, "category")).block();
        final Question question1 = new Question(null, "text", image.getId(), image, Set.of(category.getId()), List.of(category), null, null, 0, 0);
        final Question question2 = new Question(null, "text", image.getId(), image, Set.of(category.getId()), List.of(category), null, null, 0, 0);
        Question dbQuestion1 = questionRepository.save(question1).block();
        Question dbQuestion2 = questionRepository.save(question2).block();
        questionRepository.linkCategories(dbQuestion1.getId(), Set.of(category.getId())).block();
        questionRepository.linkPreviousQuestions(dbQuestion1.getId(), Set.of(dbQuestion2.getId())).block();

        // when
        questionRepository.unlinkPreviousQuestions(dbQuestion1.getId(), Set.of(dbQuestion2.getId())).block();

        // then
        Question result = questionRepository.findById(dbQuestion1.getId()).block();
        assertNotNull(result);
        Set<Long> previousQuestionsId = result.getPreviousQuestionsId();
        assertNotNull(previousQuestionsId);
        assertTrue(previousQuestionsId.isEmpty());
    }

    @Test
    void getQuestionCategoriesId_whenCategoriesExist_thenReturnCategoriesId() {
        // given
        Image image = imageRepository.save(new Image(null, new byte[]{}, LocalDateTime.now())).block();
        Category category = categoryRepository.save(new Category(null, "category")).block();
        Question question = questionRepository.save(new Question(null, "text", image.getId(), image, Set.of(category.getId()), List.of(category), null, null, 0, 0)).block();

        // when
        List<Long> categoriesId = questionRepository.getQuestionCategoriesId(question.getId()).collectList().block();

        // then
        assertNotNull(categoriesId);
        assertEquals(1, categoriesId.size());
    }

    @Test
    void deleteAll_whenThereIsAtLeastOneQuestion_thenShouldBeDeleted() {
        // given
        Image image = imageRepository.save(new Image(null, new byte[]{}, LocalDateTime.now())).block();
        Category category = categoryRepository.save(new Category(null, "category")).block();
        questionRepository.save(new Question(null, "text", image.getId(), image, Set.of(category.getId()), List.of(category), null, null, 0, 0)).block();

        // when
        questionRepository.deleteAll().block();

        // then
        List<Question> result = questionRepository.findAll().collectList().block();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}