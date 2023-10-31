package org.babi.backend.question.dao;

import org.babi.backend.category.domain.Category;
import org.babi.backend.question.domain.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
        return databaseClient.sql("select q.id, q.text, q.icon_id, qc.category_id, c.name, qt.previous_question_id " +
                        "from question q " +
                        "join question_category qc " +
                        "on q.id = qc.question_id " +
                        "join category c " +
                        "on qc.category_id = c.id " +
                        "left join question_tree qt " +
                        "on q.id = qt.question_id")
                .map((row, rowMetadata) -> new QuestionCategoryRow(
                        row.get("id", Long.class),
                        row.get("text", String.class),
                        row.get("icon_id", Long.class),
                        row.get("category_id", Long.class),
                        row.get("name", String.class),
                        row.get("previous_question_id", Long.class)
                ))
                .all()
                .collectList()
                .map(questionCategoryRows -> questionCategoryRows.stream().collect(Collectors.groupingBy(QuestionCategoryRow::getId)))
                .flatMapMany(questions -> Flux.fromStream(questions.values().stream().map(questionCategoryRows -> {
                    Question question = new Question();
                    List<Category> categories = new ArrayList<>();
                    List<Long> categoriesId = new ArrayList<>();
                    List<Long> previousQuestionId = new ArrayList<>();
                    questionCategoryRows.forEach(questionCategoryRow -> {
                        question.setId(questionCategoryRow.getId());
                        question.setText(questionCategoryRow.getText());
                        question.setIconId(questionCategoryRow.getIconId());
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
                })));
    }

    private Question fromQuestionCategoryRows(List<QuestionCategoryRow> questionCategoryRows) {
        Question question = new Question();
        List<Category> categories = new ArrayList<>();
        List<Long> categoriesId = new ArrayList<>();
        questionCategoryRows.forEach(questionCategoryRow -> {
            categories.add(new Category(questionCategoryRow.getCategoryId(), questionCategoryRow.getCategoryName()));
            categoriesId.add(questionCategoryRow.getCategoryId());
        });
        question.setCategoriesId(categoriesId);
        question.setCategories(categories);

        return question;
    }

    @Override
    public Mono<Question> save(Question question) {
        return databaseClient.sql("insert into question(text, icon_id) values (:text, :iconId)")
                .bind("text", question.getText())
                .bind("iconId", question.getIconId())
                .filter((statement, executeFunction) -> statement.returnGeneratedValues("id").execute())
                .fetch().first()
                .doOnNext(result -> question.setId(Long.parseLong(result.get("id").toString())))
                .thenReturn(question);

    }

    @Override
    public Mono<Question> update(Question question) {
        return databaseClient.sql("update question set text = :text, icon_id = :iconId where id = :id")
                .bind("text", question.getText())
                .bind("iconId", question.getIconId())
                .bind("id", question.getId())
                .fetch()
                .all()
                .then(Mono.just(question));
    }

    @Override
    public Mono<Long> linkCategories(Long questionId, List<Long> categories) {
        String sql = buildLinkCategoriesSQL(questionId, categories);
        return databaseClient.sql(sql)
                .fetch()
                .all()
                .then(Mono.just(questionId));
    }

    private String buildLinkCategoriesSQL(Long questionId, List<Long> categories) {
        final StringBuilder sql = new StringBuilder("insert into question_category(question_id, category_id) values ");
        categories.forEach(categoryId -> sql.append(String.format("(%d, %d), ", questionId, categoryId)));
        final int length = sql.length();
        return sql.replace(length - 2, length, "").toString();
    }

    @Override
    public Mono<Long> unlinkCategories(Long questionId, List<Long> categoriesId) {
        return databaseClient.sql(buildUnlinkCategories(questionId, categoriesId))
                .bind("questionId", questionId)
                .fetch()
                .all()
                .then(Mono.just(questionId));
    }

    private String buildUnlinkCategories(Long questionId, List<Long> categoriesId) {
        final StringBuilder sql = new StringBuilder("delete from question_category where question_id = :questionId and category_id in (");
        categoriesId.forEach(categoryId -> sql.append(String.format("%d, ", categoryId)));
        final int length = sql.length();
        return sql.replace(length - 2, length, ")").toString();
    }

    @Override
    public Mono<Void> linkPreviousQuestion(Long questionId, Long previousQuestionId) {
        return databaseClient.sql("insert into question_tree(question_id, previous_question_id) values (:questionId, :previousQuestionId)")
                .bind("questionId", questionId)
                .bind("previousQuestionId", previousQuestionId)
                .fetch()
                .all()
                .then();
    }

    @Override
    public Mono<Void> unlinkPreviousQuestion(Long questionId, Long previousQuestionId) {
        return databaseClient.sql("delete from question_tree where question_id = :questionId and previous_question_id = :previousQuestionId")
                .bind("questionId", questionId)
                .bind("previousQuestionId", previousQuestionId)
                .fetch()
                .all()
                .then();
    }

    @Override
    public Flux<Long> getQuestionCategoriesId(Long questionId) {
        return databaseClient.sql("select category_id from question_category where question_id = :questionId")
                .bind("questionId", questionId)
                .map((row, rowMetadata) -> row.get("category_id", Long.class))
                .all();
    }

    private static class QuestionCategoryRow {
        private Long id;
        private String text;
        private Long iconId;
        private Long categoryId;
        private String categoryName;
        private Long previousQuestionId;

        public QuestionCategoryRow(Long id, String text, Long iconId, Long categoryId,
                                   String categoryName, Long previousQuestionId) {
            this.id = id;
            this.text = text;
            this.iconId = iconId;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.previousQuestionId = previousQuestionId;
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

        public void setPreviousQuestionId(Long previousQuestionId) {
            this.previousQuestionId = previousQuestionId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            QuestionCategoryRow that = (QuestionCategoryRow) o;
            return Objects.equals(id, that.id) && Objects.equals(text, that.text) && Objects.equals(iconId, that.iconId) && Objects.equals(categoryId, that.categoryId) && Objects.equals(categoryName, that.categoryName) && Objects.equals(previousQuestionId, that.previousQuestionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, text, iconId, categoryId, categoryName, previousQuestionId);
        }
    }

}
