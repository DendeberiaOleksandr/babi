package org.babi.backend.question.service;

import org.babi.backend.question.domain.Question;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface QuestionService {

    Flux<Question> findAll();
    Mono<Question> findById(Long id);
    Mono<Long> save(Question question);
    Mono<Long> update(Question question);
    Flux<Long> updateAll(List<Question> questions);
}
