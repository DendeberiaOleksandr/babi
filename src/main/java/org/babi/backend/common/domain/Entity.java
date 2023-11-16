package org.babi.backend.common.domain;

import org.babi.backend.common.domain.event.EntityChangedEvent;
import org.babi.backend.common.domain.event.EntityRemovedEvent;
import org.babi.backend.common.domain.event.EntitySavedEvent;

public interface Entity<T> {
    T getId();

    Class<? extends EntityRemovedEvent> getEntityRemovedEventClass();
    Class<? extends EntitySavedEvent> getEntitySavedEventClass();
    Class<? extends EntityChangedEvent> getEntityChangedEventClass();
}
