package org.babi.backend.question.api;

import org.babi.backend.question.domain.Question;
import org.babi.backend.question.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @Secured("isAuthenticated()")
    @GetMapping
    public Flux<Question> findAll() {
        return questionService.findAll();
    }

    @Secured("isAuthenticated()")
    @GetMapping("/{questionId}")
    public Mono<Question> findById(@PathVariable Long questionId) {
        return questionService.findById(questionId);
    }

    @Secured("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Mono<ResponseEntity<?>> save(@RequestBody Question question) {
        return questionService.save(question).map(ResponseEntity::ok);
    }

    @Secured("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<?>> update(@PathVariable Long id, @RequestBody Question question) {
        return questionService.update(question).map(ResponseEntity::ok);
    }

    @Secured("hasRole('ADMIN')")
    @PutMapping
    public Flux<Long> updateAll(@RequestBody List<Question> question) {
        return questionService.updateAll(question);
    }

    @Secured("hasRole('ADMIN')")
    @PutMapping("/{questionId}/previousQuestions")
    public Mono<ResponseEntity<?>> addPreviousQuestion(@PathVariable("questionId") Long questionId, @RequestBody Set<Long> previousQuestionsId) {
        return questionService.addPreviousQuestions(questionId, previousQuestionsId).map(ResponseEntity::ok);
    }

    @Secured("hasRole('ADMIN')")
    @DeleteMapping("/{questionId}/previousQuestions/{previousQuestionId}")
    public Mono<ResponseEntity<?>> removePreviousQuestion(@PathVariable("questionId") Long questionId, @PathVariable("previousQuestionId") Set<Long> previousQuestionsId) {
        return questionService.addPreviousQuestions(questionId, previousQuestionsId).map(ResponseEntity::ok);
    }

}
