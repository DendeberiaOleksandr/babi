package org.babi.backend.category.domain.event;

import org.babi.backend.category.domain.Category;
import org.babi.backend.common.domain.event.EntityChangedEvent;

public class CategoryChangedEvent extends EntityChangedEvent<Category> {
    protected CategoryChangedEvent(Category category) {
        super(category);
    }
}
