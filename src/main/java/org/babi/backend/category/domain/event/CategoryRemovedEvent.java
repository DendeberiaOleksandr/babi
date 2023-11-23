package org.babi.backend.category.domain.event;

import org.babi.backend.category.domain.Category;
import org.babi.backend.common.domain.event.EntityRemovedEvent;

public class CategoryRemovedEvent extends EntityRemovedEvent<Category> {
    protected CategoryRemovedEvent(Category category) {
        super(category);
    }
}
