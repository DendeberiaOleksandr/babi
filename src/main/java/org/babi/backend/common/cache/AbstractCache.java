package org.babi.backend.common.cache;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.babi.backend.common.dao.ReactiveRepository;
import org.babi.backend.common.domain.Entity;
import org.babi.backend.common.domain.event.EntityEvent;
import org.babi.backend.common.domain.event.EntityRemovedEvent;
import org.babi.backend.common.domain.event.EntitySavedEvent;
import org.checkerframework.checker.nullness.qual.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
public abstract class AbstractCache<ID, K extends Entity<ID>> implements Cache<ID, K> {

    private final CacheLoader<ID, K> asyncCacheLoader;
    protected final LoadingCache<ID, K> cache;
    private final ReactiveRepository<ID, K> repository;

    public AbstractCache(ReactiveRepository<ID, K> repository) {
        this.repository = repository;
        asyncCacheLoader = new CacheLoader<ID, K>() {
            @Override
            public @Nullable K load(ID id) throws Exception {
                return repository.findById(id).block();
            }
        };
        cache = Caffeine.newBuilder()
                .build(asyncCacheLoader);
    }

    @PostConstruct
    public void init() {
        repository.findAll(null)
                .flatMap(this::put)
                .then()
                .block();
        log.info("Cache loaded for {}. Size: {}", this.getClass().getCanonicalName(), cache.asMap().size());
    }

    @Override
    public Flux<K> getAll() {
        return Flux.fromStream(cache.asMap()
                .values()
                .stream());
    }

    @Override
    public Flux<K> getAllById(Set<? extends ID> ids) {
        return Flux.fromStream(
                cache.getAll(ids).values().stream()
        );
    }

    @Override
    public Mono<K> findById(ID id) {
        return Mono.just(
                cache.get(id)
        );
    }

    @Override
    public Mono<K> put(K k) {
        return Mono.just(k)
                .map(entity -> {
                    cache.put(k.getId(), k);
                    return entity;
                });
    }

    @Override
    public Mono<K> remove(ID id) {
        return Mono.just(cache)
                .map(LoadingCache::asMap)
                .map(map -> map.remove(id));
    }

    public abstract Mono<Void> handleEntitySavedEvent(EntitySavedEvent<? extends K> entitySavedEvent);
    public abstract Mono<Void> handleEntityRemovedEvent(EntityRemovedEvent<? extends K> entityRemovedEvent);
    protected Mono<? extends K> getEntityFromEvent(EntityEvent<? extends K> entityEvent) {
        return Mono.just(entityEvent).map(EntityEvent::getEntity);
    }

}
