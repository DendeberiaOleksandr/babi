package org.babi.backend.question.dao;

import org.babi.backend.category.domain.Category;
import org.babi.backend.common.dao.DaoUtil;
import org.babi.backend.common.exception.ResourceNotFoundException;
import org.babi.backend.question.domain.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class QuestionRepositoryImpl implements QuestionRepository {

    private final DatabaseClient databaseClient;

    @Autowired
    public QuestionRepositoryImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }


    @Override
    public Flux<Question> findAll() {
        return findAll(null);
    }

    @Override
    public Flux<Long> findPreviousQuestionsId(Long questionId) {
        return databaseClient.sql("select previous_question_id from question_tree where question_id = :questionId")
                .bind("questionId", questionId)
                .map((row, rowMetadata) -> row.get("previous_question_id", Long.class))
                .all();
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
        Map<String, Object> args = new HashMap<>();
        if (questionCriteria != null) {
            args = questionCriteria.mapCriteriaToQueryArgs(sql);
        }
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql.toString());

        for (Map.Entry<String, Object> arg : args.entrySet()) {
            executeSpec = executeSpec.bind(arg.getKey(), arg.getValue());
        }

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
    public Mono<Question> update(Question question) {
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

    @Override
    public Mono<Long> linkCategories(Long questionId, Set<Long> categories) {
        if (CollectionUtils.isEmpty(categories)) {
            return Mono.just(questionId);
        }
        String sql = buildLinkCategoriesSQL(questionId, categories);
        return databaseClient.sql(sql)
                .fetch()
                .all()
                .then(Mono.just(questionId));
    }

    private String buildLinkCategoriesSQL(Long questionId, Set<Long> categories) {
        return DaoUtil.buildLinkQuery("question_category", questionId, categories, "question_id", "category_id");
    }

    @Override
    public Mono<Long> unlinkCategories(Long questionId, Set<Long> categoriesId) {
        if (CollectionUtils.isEmpty(categoriesId)) {
            return Mono.just(questionId);
        }
        return databaseClient.sql(buildUnlinkCategories(categoriesId))
                .bind("questionId", questionId)
                .fetch()
                .all()
                .then(Mono.just(questionId));
    }

    @Override
    public Mono<Long> unlinkCategories(Long questionId) {
        return databaseClient.sql("delete from question_category where question_id = :id")
                .bind("id", questionId)
                .fetch()
                .all()
                .then(Mono.just(questionId));
    }

    private String buildUnlinkCategories(Set<Long> categoriesId) {
        return DaoUtil.buildUnlinkQuery("question_category", categoriesId, "question_id", "questionId", "category_id");
    }

    @Override
    public Mono<Long> linkPreviousQuestions(Long questionId, Set<Long> previousQuestionId) {
        if (CollectionUtils.isEmpty(previousQuestionId)) {
            return Mono.just(questionId);
        }
        return databaseClient.sql(buildLinkPreviousQuestions(questionId, previousQuestionId))
                .fetch()
                .all()
                .then(Mono.just(questionId));
    }

    private String buildLinkPreviousQuestions(Long questionId, Set<Long> previousQuestionsId) {
        return DaoUtil.buildLinkQuery("question_tree", questionId, previousQuestionsId, "question_id", "previous_question_id");
    }

    @Override
    public Mono<Long> unlinkPreviousQuestions(Long questionId, Set<Long> previousQuestionsId) {
        if (CollectionUtils.isEmpty(previousQuestionsId)) {
            return Mono.just(questionId);
        }
        return databaseClient.sql(buildUnlinkPreviousQuestions(previousQuestionsId))
                .bind("questionId", questionId)
                .fetch()
                .all()
                .then(Mono.just(questionId));
    }

    @Override
    public Mono<Long> unlinkPreviousQuestions(Long questionId) {
        return databaseClient.sql("delete from question_tree where question_id = :id")
                .bind("id", questionId)
                .fetch()
                .all()
                .then(Mono.just(questionId));
    }

    private String buildUnlinkPreviousQuestions(Set<Long> previousQuestionsId) {
        return DaoUtil.buildUnlinkQuery("question_tree", previousQuestionsId, "question_id", "questionId", "previous_question_id");
    }

    @Override
    public Flux<Long> getQuestionCategoriesId(Long questionId) {
        return databaseClient.sql("select category_id from question_category where question_id = :questionId")
                .bind("questionId", questionId)
                .map((row, rowMetadata) -> row.get("category_id", Long.class))
                .all();
    }

    @Override
    public Mono<Void> deleteAll() {
        return databaseClient.sql("delete from question_tree")
                .flatMap(result -> databaseClient.sql("delete from question_category").then())
                .flatMap(unused -> databaseClient.sql("delete from question").then())
                .then();

    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return databaseClient.sql("delete from question_tree where question_id = :questionId")
                .bind("questionId", id)
                .flatMap(result -> databaseClient.sql("delete from question_category where question_id = :id").bind("id", id).then())
                .flatMap(unused -> databaseClient.sql("delete from question where id = :id").bind("id", id).then())
                .then();
    }

    private static class QuestionCategoryRow {
        private Long id;
        private String text;
        private Long iconId;
        private Long categoryId;
        private String categoryName;
        private Long previousQuestionId;
        private int x;
        private int y;

        public QuestionCategoryRow(Long id, String text, Long iconId, Long categoryId,
                                   String categoryName, Long previousQuestionId, int x, int y) {
            this.id = id;
            this.text = text;
            this.iconId = iconId;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.previousQuestionId = previousQuestionId;
            this.x = x;
            this.y = y;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Long getIconId() {
            return iconId;
        }

        public void setIconId(Long iconId) {
            this.iconId = iconId;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public Long getPreviousQuestionId() {
            return previousQuestionId;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public void setPreviousQuestionId(Long previousQuestionId) {
            this.previousQuestionId = previousQuestionId;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            QuestionCategoryRow that = (QuestionCategoryRow) o;
            return x == that.x && y == that.y && Objects.equals(id, that.id) && Objects.equals(text, that.text) && Objects.equals(iconId, that.iconId) && Objects.equals(categoryId, that.categoryId) && Objects.equals(categoryName, that.categoryName) && Objects.equals(previousQuestionId, that.previousQuestionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, text, iconId, categoryId, categoryName, previousQuestionId, x, y);
        }
    }

}
