package org.babi.backend.question.service;

import lombok.extern.slf4j.Slf4j;
import org.babi.backend.category.service.CategoryService;
import org.babi.backend.common.exception.ResourceNotFoundException;
import org.babi.backend.question.dao.QuestionRepository;
import org.babi.backend.question.domain.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    private final CategoryService categoryService;
    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionServiceImpl(CategoryService categoryService,
                               QuestionRepository questionRepository) {
        this.categoryService = categoryService;
        this.questionRepository = questionRepository;
    }

    @Override
    public Flux<Question> findAll() {
        return questionRepository.findAll();
    }

    @Override
    public Mono<Question> findById(Long id) {
        return questionRepository.findById(id);
    }

    @Transactional
    @Override
    public Mono<Long> save(Question question) {
        return questionRepository.save(question).map(Question::getId)
                .flatMap(questionId -> categoryService.findAllById(question.getCategoriesId()).all(category -> question.getCategoriesId().contains(category.getId())))
                .flatMap(isCategoriesIdValid -> isCategoriesIdValid ? Mono.just(question.getId()) : Mono.error(new ResourceNotFoundException("Some of the provided categories does not exist")))
                .flatMap(questionId -> questionRepository.linkCategories(questionId, question.getCategoriesId()).thenReturn(questionId));
    }

    @Transactional
    @Override
    public Mono<Long> update(Question question) {
        return questionRepository.update(question)
                .flatMap(this::updateQuestionCategories)
                .flatMap(questionId -> updateQuestionPreviousQuestions(question));
    }

    private Mono<Long> updateQuestionCategories(Question question) {
        return questionRepository.findById(question.getId())
                .flatMap(dbQuestion -> questionRepository.getQuestionCategoriesId(question.getId()).collectList().flatMap(dbCategoriesId -> {
                            Set<Long> categoriesId = question.getCategoriesId();
                            Set<Long> oldCategoriesId = dbCategoriesId.stream()
                                    .filter(dbCategoryId -> !categoriesId.contains(dbCategoryId)).collect(Collectors.toSet());
                            if (oldCategoriesId.isEmpty()) {
                                return Mono.just(dbQuestion.getId());
                            }
                            return questionRepository.unlinkCategories(question.getId(), oldCategoriesId);
                        }).map(questionId -> dbQuestion.getCategoriesId())
                )
                .flatMap(oldCategoriesId -> questionRepository.getQuestionCategoriesId(question.getId()).collectList())
                .flatMap(dbCategoriesId -> {
                    Set<Long> categoriesIdToLink = question.getCategoriesId().stream().filter(newCategoryId -> !dbCategoriesId.contains(newCategoryId)).collect(Collectors.toSet());
                    if (categoriesIdToLink.isEmpty()) {
                        return Mono.just(question.getId());
                    }
                    return questionRepository.linkCategories(question.getId(),
                            categoriesIdToLink);
                });
    }

    private Mono<Long> updateQuestionPreviousQuestions(Question question) {
        return questionRepository.findById(question.getId())
                .flatMap(dbQuestion -> {
                    Set<Long> previousQuestionsId = question.getPreviousQuestionsId();
                    Set<Long> oldPreviousQuestionsId = dbQuestion.getPreviousQuestionsId().stream()
                            .filter(dbPrevQuestionId -> !previousQuestionsId.contains(dbPrevQuestionId))
                            .collect(Collectors.toSet());
                    if (oldPreviousQuestionsId.isEmpty()) {
                        return Mono.just(dbQuestion);
                    }
                    return questionRepository.unlinkPreviousQuestions(dbQuestion.getId(), oldPreviousQuestionsId)
                            .map(questionId -> dbQuestion.getPreviousQuestionsId());
                })
                .flatMap(oldPreviousQuestionsId -> questionRepository.findPreviousQuestionsId(question.getId()).collectList())
                .flatMap(dbPreviousQuestionsId -> {
                    Set<Long> previousQuestionsIdToLink = question.getPreviousQuestionsId().stream().filter(newPreviousQuestionId -> !dbPreviousQuestionsId.contains(newPreviousQuestionId)).collect(Collectors.toSet());
                    if(previousQuestionsIdToLink.isEmpty()) {
                        return Mono.just(question.getId());
                    }
                    return questionRepository.linkPreviousQuestions(question.getId(), previousQuestionsIdToLink);
                });
    }

    @Transactional
    @Override
    public Flux<Long> updateAll(List<Question> questions) {
        return Flux.fromStream(questions.stream())
                .flatMap(this::update);
    }

    @Transactional
    @Override
    public Mono<Long> addPreviousQuestions(Long questionId, Set<Long> previousQuestionsId) {
        return questionRepository.linkPreviousQuestions(questionId, previousQuestionsId);
    }

    @Transactional
    @Override
    public Mono<Long> removePreviousQuestions(Long questionId, Set<Long> previousQuestionsId) {
        return questionRepository.unlinkPreviousQuestions(questionId, previousQuestionsId);
    }


}
