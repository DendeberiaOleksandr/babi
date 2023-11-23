package org.babi.backend.common.domain.event;

import org.babi.backend.common.domain.Entity;

public abstract class EntityChangedEvent<T extends Entity<?>> extends EntityEvent<T> {
    protected EntityChangedEvent(T entity) {
        super(entity);
    }
}
