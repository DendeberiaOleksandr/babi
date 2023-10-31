package org.babi.backend.question.api;

import org.babi.backend.question.domain.Question;
import org.babi.backend.question.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Secured("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Mono<ResponseEntity<?>> save(@RequestBody Question question) {
        return questionService.save(question).map(ResponseEntity::ok);
    }

    @Secured("hasRole('ADMIN')")
    @PutMapping
    public Mono<ResponseEntity<?>> update(@RequestBody Question question) {
        return questionService.update(question).map(ResponseEntity::ok);
    }

    @Secured("hasRole('ADMIN')")
    @PutMapping("/{questionId}/previousQuestions")
    public Mono<ResponseEntity<?>> addPreviousQuestion(@PathVariable("questionId") Long questionId, @RequestBody Long previousQuestionId) {
        return questionService.addPreviousQuestion(questionId, previousQuestionId).map(ResponseEntity::ok);
    }

    @Secured("hasRole('ADMIN')")
    @DeleteMapping("/{questionId}/previousQuestions/{previousQuestionId}")
    public Mono<ResponseEntity<?>> removePreviousQuestion(@PathVariable("questionId") Long questionId, @PathVariable("previousQuestionId") Long previousQuestionId) {
        return questionService.addPreviousQuestion(questionId, previousQuestionId).map(ResponseEntity::ok);
    }

}
