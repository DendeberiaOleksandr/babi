package org.babi.backend.question.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.babi.backend.category.domain.Category;
import org.babi.backend.common.domain.Entity;
import org.babi.backend.common.domain.event.EntityChangedEvent;
import org.babi.backend.common.domain.event.EntityRemovedEvent;
import org.babi.backend.common.domain.event.EntitySavedEvent;
import org.babi.backend.image.domain.Image;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = { "image", "categories", "previousQuestions" })
@EqualsAndHashCode(exclude = { "image", "categories",  "previousQuestions" })
@Builder
public class Question implements Entity<Long> {

    private Long id;
    private String text;

    private Long iconId;
    private Image image;

    private Set<Long> categoriesId;
    private List<Category> categories;

    private Set<Long> previousQuestionsId;
    private List<Question> previousQuestions;
    private int x;
    private int y;
}
