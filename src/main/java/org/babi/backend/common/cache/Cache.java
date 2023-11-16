package org.babi.backend.common.cache;

import org.babi.backend.common.domain.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface Cache<ID, K extends Entity<ID>> {

    Flux<K> getAll();
    Flux<K> getAllById(Set<? extends ID> ids);
    Mono<K> findById(ID id);
    Mono<K> put(K k);
    Mono<K> remove(ID id);

}
