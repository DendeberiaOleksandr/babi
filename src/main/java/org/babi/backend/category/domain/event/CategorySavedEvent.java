package org.babi.backend.category.domain.event;

import org.babi.backend.category.domain.Category;
import org.babi.backend.common.domain.event.EntitySavedEvent;

public class CategorySavedEvent extends EntitySavedEvent<Category> {
    public CategorySavedEvent(Category category) {
        super(category);
    }
}
