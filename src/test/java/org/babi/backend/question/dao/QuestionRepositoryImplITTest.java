package org.babi.backend.question.dao;

import org.babi.backend.category.dao.CategoryRepository;
import org.babi.backend.category.domain.Category;
import org.babi.backend.dao.AbstractDaoITTest;
import org.babi.backend.image.dao.ImageRepository;
import org.babi.backend.image.domain.Image;
import org.babi.backend.question.domain.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class QuestionRepositoryImplITTest extends AbstractDaoITTest {

    private QuestionRepository questionRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DatabaseClient databaseClient;


    @BeforeEach
    void setUp() {
        questionRepository = new QuestionRepositoryImpl(databaseClient);
    }

    @Test
    void findAll_whenThereAreNoQuestions_thenExpectEmpty() {
        // given

        // when
        Flux<Question> actual = questionRepository.findAll();

        // then
        StepVerifier.create(actual)
                .expectComplete()
                .verify();
    }

    @ParameterizedTest
    @MethodSource("provideQuestionsForeignKeyViolation")
    void save_whenProvidedForeignEntityKeyDoesNotExist_thenExpectException(Question question) {
        // given

        // when
        Flux<Question> actual = questionRepository.save(question)
                .flatMapMany(q -> questionRepository.findAll());

        // then
        StepVerifier.create(actual)
                .expectError(DataIntegrityViolationException.class)
                .verify();
    }

    private static Stream<Arguments> provideQuestionsForeignKeyViolation() {
        return Stream.of(
                Arguments.of(new Question(null, "text", 1L, null, null, null, null, null))
        );
    }

    @Test
    void save_whenValidQuestionProvided_thenExpectSaved() {
        // given
        final Image image = new Image(null, new byte[]{}, LocalDateTime.now());
        final Category category = new Category(null, "category");
        final Question question = new Question(null, "text", null, image, null, List.of(category), null, null);

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
                    question.setCategoriesId(List.of(categoryId));
                    return questionRepository.save(question);
                })
                .flatMap(q -> questionRepository.linkCategories(q.getId(), q.getCategoriesId()))
                .block();

        Question result = questionRepository.findAll().blockFirst();


        // then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getIconId());
        List<Long> categoriesId = result.getCategoriesId();
        assertNotNull(categoriesId);
        assertFalse(categoriesId.isEmpty());
        List<Category> categories = result.getCategories();
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
    }

}