package org.babi.backend.common.domain.event;

import org.babi.backend.common.domain.Entity;

public abstract class EntityRemovedEvent<T extends Entity<?>> extends EntityEvent<T> {

    protected EntityRemovedEvent(T t) {
        super(t);
    }
}
