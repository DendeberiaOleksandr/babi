package org.babi.backend.common.service;

import org.babi.backend.common.domain.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface ReactiveService<ID, T extends Entity<ID>> {

    Flux<T> findAll();
    Flux<T> findAllById(Set<? extends ID> ids);
    Mono<T> findById(ID id);
    Mono<T> save(T t);
    default Mono<T> save(Mono<T> t) {
        return t.flatMap(this::save);
    }
    Mono<T> update(ID id, T t);
    Mono<T> removeById(ID id);

}
