package org.babi.backend.question.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.babi.backend.category.domain.Category;
import org.babi.backend.common.dao.AbstractRepository;
import org.babi.backend.common.dao.Criteria;
import org.babi.backend.common.dao.PageableResponse;
import org.babi.backend.common.exception.ResourceNotFoundException;
import org.babi.backend.question.domain.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.testcontainers.shaded.com.google.common.annotations.VisibleForTesting;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class QuestionRepositoryImpl extends AbstractRepository<Long, Question> implements QuestionRepository {

    @Autowired
    public QuestionRepositoryImpl(DatabaseClient databaseClient) {
        super(databaseClient);
    }

    @Override
    public Mono<PageableResponse<Question>> search(Criteria criteria) {
        return findAll((QuestionCriteria) criteria)
                .collectList()
                .flatMap(questions -> count(criteria).map(count -> new PageableResponse<>(questions, count)));
    }

    @Override
    public Mono<Long> count(Criteria criteria) {
        final StringBuilder sql = new StringBuilder("select count(distinct q.id) " +
                "from question q " +
                "join question_category qc " +
                "on q.id = qc.question_id " +
                "join category c " +
                "on qc.category_id = c.id " +
                "left join question_tree qt " +
                "on q.id = qt.question_id");
        return count(sql, criteria);
    }

    @Override
    public Flux<Question> findAll() {
        return findAll(null);
    }

    private Flux<Question> findAll(QuestionCriteria questionCriteria) {
        final StringBuilder sql = new StringBuilder("select q.id, q.text, q.icon_id, q.x, q.y, qc.category_id, c.name, qt.previous_question_id " +
                "from question q " +
                "join question_category qc " +
                "on q.id = qc.question_id " +
                "join category c " +
                "on qc.category_id = c.id " +
                "left join question_tree qt " +
                "on q.id = qt.question_id");

        DatabaseClient.GenericExecuteSpec executeSpec = executeSpecFilledByArgs(sql, questionCriteria);

        return executeSpec
                .map((row, rowMetadata) -> new QuestionCategoryRow(
                        row.get("id", Long.class),
                        row.get("text", String.class),
                        row.get("icon_id", Long.class),
                        row.get("category_id", Long.class),
                        row.get("name", String.class),
                        row.get("previous_question_id", Long.class),
                        Optional.ofNullable(row.get("x", Integer.class)).orElse(0),
                        Optional.ofNullable(row.get("y", Integer.class)).orElse(0)
                ))
                .all()
                .collectList()
                .map(questionCategoryRows -> questionCategoryRows.stream().collect(Collectors.groupingBy(QuestionCategoryRow::getId)))
                .flatMapMany(questions -> Flux.fromStream(questions.values().stream().map(questionCategoryRows -> {
                    Question question = new Question();
                    List<Category> categories = new ArrayList<>();
                    Set<Long> categoriesId = new HashSet<>();
                    Set<Long> previousQuestionId = new HashSet<>();
                    questionCategoryRows.forEach(questionCategoryRow -> {
                        question.setId(questionCategoryRow.getId());
                        question.setText(questionCategoryRow.getText());
                        question.setIconId(questionCategoryRow.getIconId());
                        question.setX(questionCategoryRow.getX());
                        question.setY(questionCategoryRow.getY());
                        categories.add(new Category(questionCategoryRow.getCategoryId(), questionCategoryRow.getCategoryName()));
                        categoriesId.add(questionCategoryRow.getCategoryId());

                        Long previousId = questionCategoryRow.getPreviousQuestionId();
                        if (previousId != null) {
                            previousQuestionId.add(previousId);
                        }
                    });
                    question.setCategories(categories);
                    question.setCategoriesId(categoriesId);
                    question.setPreviousQuestionsId(previousQuestionId);

                    return question;
                })))
                .collectList()
                .map(questions -> {
                    Map<Long, Question> questionMap = questions.stream().collect(Collectors.toMap(Question::getId, question -> question));
                    questionMap.values()
                            .forEach(question -> question.setPreviousQuestions(question.getPreviousQuestionsId().stream().map(questionMap::get).collect(Collectors.toList())));
                    return questionMap.values();
                }).flatMapMany(questions -> Flux.fromStream(questions.stream()));
    }

    @Override
    public Mono<Question> findById(Long id) {
        return findAll(new QuestionCriteria(id)).switchIfEmpty(Mono.error(new ResourceNotFoundException(Question.class, "id", id))).single();
    }

    @Override
    public Flux<Question> findAllById(Set<? extends Long> id) {
        return null;
    }

    @Override
    public Mono<Question> save(Question question) {
        return databaseClient.sql("insert into question(text, icon_id) values (:text, :iconId)")
                .bind("text", question.getText())
                .bind("iconId", question.getIconId())
                .filter((statement, executeFunction) -> statement.returnGeneratedValues("id").execute())
                .fetch().first()
                .doOnNext(result -> question.setId(Long.parseLong(result.get("id").toString())))
                .flatMap(result -> linkCategories(question.getId(), question.getCategoriesId()))
                .flatMap(questionId -> linkPreviousQuestions(questionId, question.getPreviousQuestionsId()))
                .thenReturn(question);

    }

    @Override
    public Mono<Question> update(Long aLong, Question question) {
        return update(question);
    }

    private Mono<Question> update(Question question) {
        return databaseClient.sql("update question set text = :text, icon_id = :iconId, x = :x, y = :y where id = :id")
                .bind("text", question.getText())
                .bind("iconId", question.getIconId())
                .bind("id", question.getId())
                .bind("x", question.getX())
                .bind("y", question.getY())
                .fetch()
                .all()
                .then(Mono.just(question))
                .flatMap(q -> unlinkCategories(q.getId()))
                .flatMap(questionId -> linkCategories(questionId, question.getCategoriesId()))
                .flatMap(this::unlinkPreviousQuestions)
                .flatMap(questionId -> linkPreviousQuestions(questionId, question.getPreviousQuestionsId()))
                .thenReturn(question);
    }

    @VisibleForTesting
    Flux<Long> findPreviousQuestionsId(Long questionId) {
        return findAllNestedIds(QuestionTreeTable.TABLE, QuestionTreeTable.QUESTION_ID, questionId, QuestionTreeTable.PREVIOUS_QUESTION_ID);
    }

    @VisibleForTesting
    Mono<Long> linkCategories(Long questionId, Set<Long> categoriesId) {
        return linkNestedEntities("question_category", questionId, categoriesId, "question_id", "category_id", "questionId", "categoryId");
    }

    @VisibleForTesting
    Mono<Long> unlinkCategories(Long questionId, Set<Long> categoriesId) {
        return unlinkNestedEntities("question_category", questionId, categoriesId, "question_id", "category_id");
    }

    @VisibleForTesting
    Mono<Long> unlinkCategories(Long questionId) {
        return unlinkNestedEntities(QuestionCategoryTable.TABLE, QuestionCategoryTable.QUESTION_ID, questionId);
    }

    @VisibleForTesting
    Mono<Long> linkPreviousQuestions(Long questionId, Set<Long> previousQuestionId) {
        return linkNestedEntities(QuestionTreeTable.TABLE, questionId, previousQuestionId, QuestionTreeTable.QUESTION_ID, QuestionTreeTable.PREVIOUS_QUESTION_ID);
    }

    @VisibleForTesting
    Mono<Long> unlinkPreviousQuestions(Long questionId, Set<Long> previousQuestionsId) {
        return unlinkNestedEntities(QuestionTreeTable.TABLE, questionId, previousQuestionsId, QuestionTreeTable.QUESTION_ID, QuestionTreeTable.PREVIOUS_QUESTION_ID);
    }

    @VisibleForTesting
    Mono<Long> unlinkPreviousQuestions(Long questionId) {
        return unlinkNestedEntities(QuestionTreeTable.TABLE, QuestionTreeTable.QUESTION_ID, questionId);
    }

    @VisibleForTesting
    Flux<Long> getQuestionCategoriesId(Long questionId) {
        return findAllNestedIds(QuestionCategoryTable.TABLE, QuestionCategoryTable.QUESTION_ID, questionId, QuestionCategoryTable.CATEGORY_ID);
    }

    @Override
    public Mono<Void> deleteAll() {
        return deleteAll(List.of(
                QuestionCategoryTable.TABLE, QuestionTreeTable.TABLE, QuestionTable.TABLE
        ));

    }

    @Override
    public Mono<Void> delete(Question question) {
        return delete(question.getId());
    }

    @Override
    public Mono<Void> delete(Long id) {
        return deleteById(List.of(
                new DeleteByIdParam(QuestionCategoryTable.TABLE, QuestionCategoryTable.QUESTION_ID, id),
                new DeleteByIdParam(QuestionTreeTable.TABLE, QuestionTreeTable.QUESTION_ID, id),
                new DeleteByIdParam(QuestionTable.TABLE, QuestionTable.ID, id)
        ));
    }

    @AllArgsConstructor
    @Data
    private static class QuestionCategoryRow {
        private Long id;
        private String text;
        private Long iconId;
        private Long categoryId;
        private String categoryName;
        private Long previousQuestionId;
        private int x;
        private int y;
    }

    private static class QuestionCategoryTable {
        private static final String TABLE = "question_category";
        private static final String QUESTION_ID = "question_id";
        private static final String CATEGORY_ID = "category_id";
    }

    private static class QuestionTreeTable {
        private static final String TABLE = "question_tree";
        private static final String QUESTION_ID = "question_id";
        private static final String PREVIOUS_QUESTION_ID = "previous_question_id";
    }

    private static class QuestionTable {
        private static final String TABLE = "question";
        private static final String ID = "id";
    }

}
