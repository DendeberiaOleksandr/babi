package org.babi.backend.question.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.babi.backend.category.domain.Category;
import org.babi.backend.image.domain.Image;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = { "image", "categories", "previousQuestions" })
@EqualsAndHashCode(exclude = { "image", "categories",  "previousQuestions" })
@Builder
public class Question {

    private Long id;
    private String text;

    private Long iconId;
    private Image image;

    private List<Long> categoriesId;
    private List<Category> categories;

    private List<Long> previousQuestionsId;
    private List<Question> previousQuestions;
}
