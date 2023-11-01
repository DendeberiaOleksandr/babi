package org.babi.backend.question.service;

import org.babi.backend.question.domain.Question;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface QuestionService {

    Flux<Question> findAll();
    Mono<Question> findById(Long id);
    Mono<Long> save(Question question);
    Mono<Long> update(Question question);
    Mono<Void> addPreviousQuestion(Long questionId, Long previousQuestionId);
    Mono<Void> removePreviousQuestion(Long questionId, Long previousQuestionId);
}
