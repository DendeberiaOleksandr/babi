package org.babi.backend.common.cache;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractCache<ID, K extends Entity<ID>> implements Cache<ID, K> {

    private final AsyncCacheLoader<ID, K> asyncCacheLoader;
    protected final AsyncLoadingCache<ID, K> cache;
    private final ReactiveRepository<ID, K> repository;

    public AbstractCache(ReactiveRepository<ID, K> repository) {
        this.repository = repository;
        asyncCacheLoader = new AsyncCacheLoader<ID, K>() {
            @Override
            public CompletableFuture<? extends Map<? extends ID, ? extends K>> asyncLoadAll(Set<? extends ID> keys, Executor executor) throws Exception {
                return CompletableFuture.supplyAsync(() -> repository.findAllById(keys).toStream()
                        .collect(Collectors.toMap(K::getId, k -> k)));
            }

            @Override
            public CompletableFuture<? extends K> asyncLoad(ID id, Executor executor) throws Exception {
                return repository.findById(id).toFuture();
            }
        };
        cache = Caffeine.newBuilder()
                .buildAsync(asyncCacheLoader);
    }

    @PostConstruct
    public void init() {
        repository.findAll()
                .flatMap(this::put)
                .then(Mono.fromRunnable(() -> log.info("Cache loaded for {}. Size: {}", this.getClass().getCanonicalName(), cache.asMap().size())))
                .block();
    }

    @Override
    public Flux<K> getAll() {
        return Flux.fromStream(cache.asMap().values().stream())
                .map(CompletableFuture::join);
    }

    @Override
    public Flux<K> getAllById(Set<? extends ID> ids) {
        return Flux.fromStream(
                cache.getAll(ids).join().values().stream()
        );
    }

    @Override
    public Mono<K> findById(ID id) {
        return Mono.just(
                cache.get(id).join()
        );
    }

    @Override
    public Mono<K> put(K k) {
        return Mono.just(k)
                .map(entity -> {
                    cache.put(k.getId(), Mono.just(k).toFuture());
                    return entity;
                });
    }

    @Override
    public Mono<K> remove(ID id) {
        return Mono.just(cache)
                .map(AsyncLoadingCache::asMap)
                .map(map -> map.remove(id).join());
    }

    public abstract Mono<Void> handleEntitySavedEvent(EntitySavedEvent<? extends K> entitySavedEvent);

    public abstract Mono<Void> handleEntityRemovedEvent(EntityRemovedEvent<? extends K> entityRemovedEvent);

    protected Mono<? extends K> getEntityFromEvent(EntityEvent<? extends K> entityEvent) {
        return Mono.just(entityEvent).map(EntityEvent::getEntity);
    }

}
