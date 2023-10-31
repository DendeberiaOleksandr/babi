package org.babi.backend.question.dao;

import org.babi.backend.question.domain.Question;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface QuestionRepository {

    Flux<Question> findAll();
    Mono<Question> save(Question question);
    Mono<Question> update(Question question);
    Mono<Long> linkCategories(Long questionId, List<Long> categoriesId);
    Mono<Long> unlinkCategories(Long questionId, List<Long> categoriesId);
    Mono<Void> linkPreviousQuestion(Long questionId, Long previousQuestionId);
    Mono<Void> unlinkPreviousQuestion(Long questionId, Long previousQuestionId);
    Flux<Long> getQuestionCategoriesId(Long questionId);

}
