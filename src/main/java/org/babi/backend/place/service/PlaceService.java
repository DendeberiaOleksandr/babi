package org.babi.backend.place.service;

import org.babi.backend.common.dao.PageableResponse;
import org.babi.backend.place.dao.PlaceCriteria;
import org.babi.backend.place.domain.Place;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PlaceService {

    Flux<Place> findAll();
    Mono<PageableResponse<Place>> search(PlaceCriteria placeCriteria);
    Mono<Long> count(PlaceCriteria placeCriteria);
    Mono<Place> findById(Long id);
    Mono<Long> save(Place place);
    Mono<Long> update(Place place);
    Mono<Void> deleteAll();
    Mono<Void> deleteById(Long id);

}
