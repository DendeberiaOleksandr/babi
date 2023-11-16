package org.babi.backend.common.domain.event;

import org.babi.backend.common.domain.Entity;

public abstract class EntitySavedEvent<T extends Entity<?>> extends EntityEvent<T>  {
    protected EntitySavedEvent(T t) {
        super(t);
    }
}
