package org.babi.backend.common.domain.event;

import org.babi.backend.common.domain.Entity;

public abstract class EntityEvent<T extends Entity<?>> {
    private final T t;

    protected EntityEvent(T t) {
        this.t = t;
    }

    public T getEntity() {
        return t;
    }
}
