package org.babi.backend.question.service;

import lombok.extern.slf4j.Slf4j;
import org.babi.backend.category.service.CategoryService;
import org.babi.backend.question.dao.QuestionRepository;
import org.babi.backend.question.domain.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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
        return questionRepository.save(question).map(Question::getId);
    }

    @Transactional
    @Override
    public Mono<Long> update(Question question) {
        return questionRepository.update(question).map(Question::getId);
    }

    @Transactional
    @Override
    public Flux<Long> updateAll(List<Question> questions) {
        return Flux.fromStream(questions.stream())
                .flatMap(this::update);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return questionRepository.deleteById(id);
    }


}
