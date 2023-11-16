package org.babi.backend.common.cache;

import org.babi.backend.common.domain.Entity;
import org.babi.backend.common.domain.event.EntityChangedEvent;
import org.babi.backend.common.domain.event.EntityRemovedEvent;
import org.babi.backend.common.domain.event.EntitySavedEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface Cache<ID, K extends Entity<ID>> {

    Flux<K> getAll();
    Flux<K> getAllById(Set<? extends ID> ids);
    Mono<K> findById(ID id);
    Mono<K> put(K k);
    Mono<K> remove(ID id);
    Class<? extends EntityRemovedEvent> getEntityRemovedEventClass();
    Class<? extends EntitySavedEvent> getEntitySavedEventClass();
    Class<? extends EntityChangedEvent> getEntityChangedEventClass();

}
