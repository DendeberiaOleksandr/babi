package org.babi.backend.common.dao;

import org.babi.backend.common.domain.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface ReactiveRepository<ID, K extends Entity<ID>> {

    Flux<K> findAll(Criteria criteria);
    Flux<K> findAllById(Set<? extends ID> id);
    Mono<K> findById(ID id);
    Mono<K> save(K k);
    Mono<Void> remove(ID id);
    Mono<Void> remove(K k);
    Mono<K> update(ID id, K k);
    Mono<Void> deleteAll();
}
