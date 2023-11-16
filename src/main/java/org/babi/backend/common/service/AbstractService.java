package org.babi.backend.common.service;

import lombok.extern.slf4j.Slf4j;
import org.babi.backend.common.cache.Cache;
import org.babi.backend.common.dao.ReactiveRepository;
import org.babi.backend.common.domain.Entity;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
public abstract class AbstractService<ID, T extends Entity<ID>> implements ReactiveService<ID, T> {

    private final Cache<ID, T> cache;
    private final ReactiveRepository<ID, T> repository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AbstractService(Cache<ID, T> cache,
                           ReactiveRepository<ID, T> repository,
                           ApplicationEventPublisher applicationEventPublisher) {
        this.cache = cache;
        this.repository = repository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Flux<T> findAll() {
        return cache.getAll();
    }

    @Override
    public Flux<T> findAllById(Set<? extends ID> ids) {
        return cache.getAllById(ids);
    }

    @Override
    public Mono<T> findById(ID id) {
        return cache.findById(id)
                .switchIfEmpty(repository.findById(id).flatMap(cache::put));
    }

    @Override
    public Mono<T> save(T t) {
        return repository.save(t)
                .map(savedT -> {
                    try {
                        applicationEventPublisher
                                .publishEvent(t.getEntitySavedEventClass()
                                        .getConstructor(Entity.class).newInstance(t));
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                    return savedT;
                });
    }

    @Override
    public Mono<T> removeById(ID id) {
        return findById(id)
                .flatMap(t -> cache.remove(id))
                .map(t -> {
                    try {
                        applicationEventPublisher.publishEvent(t.getEntityRemovedEventClass().getConstructor(Entity.class).newInstance(t));
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                    return t;
                });
    }

    @Override
    public Mono<T> update(ID id, T t) {
        return repository.update(id, t)
                .map(entity -> {
                    try {
                        applicationEventPublisher.publishEvent(t.getEntityChangedEventClass().getConstructor(Entity.class).newInstance(t));
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                    return entity;
                });
    }
}
