package org.babi.backend.common.dao;

import org.babi.backend.common.domain.Entity;
import reactor.core.publisher.Mono;

public interface ReactivePageableRepository<ID, T extends Entity<ID>> {

    Mono<PageableResponse<T>> search(Criteria criteria);
    Mono<Long> count(Criteria criteria);

}
