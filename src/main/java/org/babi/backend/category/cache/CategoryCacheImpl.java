package org.babi.backend.category.cache;

import org.babi.backend.category.dao.CategoryRepository;
import org.babi.backend.category.domain.Category;
import org.babi.backend.category.domain.event.CategoryChangedEvent;
import org.babi.backend.category.domain.event.CategoryRemovedEvent;
import org.babi.backend.category.domain.event.CategorySavedEvent;
import org.babi.backend.common.cache.AbstractCache;
import org.babi.backend.common.domain.event.EntityChangedEvent;
import org.babi.backend.common.domain.event.EntityRemovedEvent;
import org.babi.backend.common.domain.event.EntitySavedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CategoryCacheImpl extends AbstractCache<Long, Category> {

    @Autowired
    public CategoryCacheImpl(CategoryRepository categoryRepository) {
        super(categoryRepository);
    }

    @Async
    @EventListener(CategorySavedEvent.class)
    @Override
    public Mono<Void> handleEntitySavedEvent(EntitySavedEvent<? extends Category> categorySavedEvent) {
        return getEntityFromEvent(categorySavedEvent)
                .flatMap(super::put)
                .then();
    }

    @Async
    @EventListener(CategoryRemovedEvent.class)
    @Override
    public Mono<Void> handleEntityRemovedEvent(EntityRemovedEvent<? extends Category> entityRemovedEvent) {
        return getEntityFromEvent(entityRemovedEvent)
                .flatMap(category -> remove(category.getId()))
                .then();
    }

    @Override
    public Class<? extends EntityRemovedEvent> getEntityRemovedEventClass() {
        return CategoryRemovedEvent.class;
    }

    @Override
    public Class<? extends EntitySavedEvent> getEntitySavedEventClass() {
        return CategorySavedEvent.class;
    }

    @Override
    public Class<? extends EntityChangedEvent> getEntityChangedEventClass() {
        return CategoryChangedEvent.class;
    }
}
