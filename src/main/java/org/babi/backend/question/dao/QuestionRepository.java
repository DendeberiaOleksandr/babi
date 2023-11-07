package org.babi.backend.question.dao;

import org.babi.backend.question.domain.Question;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface QuestionRepository {

    Flux<Question> findAll();
    Flux<Long> findPreviousQuestionsId(Long questionId);
    Mono<Question> findById(Long id);
    Mono<Question> save(Question question);
    Mono<Question> update(Question question);
    Mono<Long> linkCategories(Long questionId, Set<Long> categoriesId);
    Mono<Long> unlinkCategories(Long questionId, Set<Long> categoriesId);
    Mono<Long> linkPreviousQuestions(Long questionId, Set<Long> previousQuestionsId);
    Mono<Long> unlinkPreviousQuestions(Long questionId, Set<Long> previousQuestionsId);
    Flux<Long> getQuestionCategoriesId(Long questionId);
    Mono<Void> deleteAll();

}
