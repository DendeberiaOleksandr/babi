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
                .flatMap(dbQuestion -> questionRepository.getQuestionCategoriesId(question.getId()).collectList().flatMap(dbCategoriesId -> {
                            List<Long> categoriesId = question.getCategoriesId();
                            List<Long> oldCategoriesId = dbCategoriesId.stream()
                                    .filter(dbCategoryId -> !categoriesId.contains(dbCategoryId)).collect(Collectors.toList());
                            if (oldCategoriesId.isEmpty()) {
                                return Mono.just(dbQuestion.getId());
                            }
                            return questionRepository.unlinkCategories(question.getId(), oldCategoriesId);
                        }).map(questionId -> dbQuestion.getCategoriesId())
                )
                .flatMap(oldCategoriesId -> questionRepository.getQuestionCategoriesId(question.getId()).collectList())
                .flatMap(dbCategoriesId -> {
                    List<Long> categoriesIdToLink = question.getCategoriesId().stream().filter(newCategoryId -> !dbCategoriesId.contains(newCategoryId)).collect(Collectors.toList());
                    if (categoriesIdToLink.isEmpty()) {
                        return Mono.just(question.getId());
                    }
                    return questionRepository.linkCategories(question.getId(),
                            categoriesIdToLink).thenReturn(question.getId());
                });
    }

    @Transactional
    @Override
    public Mono<Void> addPreviousQuestion(Long questionId, Long previousQuestionId) {
        return questionRepository.linkPreviousQuestion(questionId, previousQuestionId);
    }

    @Transactional
    @Override
    public Mono<Void> removePreviousQuestion(Long questionId, Long previousQuestionId) {
        return questionRepository.unlinkPreviousQuestion(questionId, previousQuestionId);
    }


}
