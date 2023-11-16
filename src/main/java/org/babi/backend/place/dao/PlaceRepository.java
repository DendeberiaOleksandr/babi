package org.babi.backend.place.dao;

import org.babi.backend.common.dao.PageableResponse;
import org.babi.backend.common.dao.ReactiveRepository;
import org.babi.backend.place.domain.Place;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface PlaceRepository extends ReactiveRepository<Long, Place> {

    Mono<PageableResponse<Place>> search(PlaceCriteria placeCriteria);
    Flux<Place> findAll();
    Mono<Long> count(PlaceCriteria placeCriteria);
    Mono<Place> findById(Long id);
    Mono<Place> save(Place place);
    Mono<Place> update(Place place);
    Mono<Long> linkCategories(Long placeId, Set<Long> categoriesId);
    Mono<Long> unlinkCategories(Long placeId, Set<Long> categoriesId);
    Mono<Long> unlinkCategories(Long placeId);
    Mono<Long> linkImages(Long placeId, Set<Long> imagesId);
    Mono<Long> unlinkImages(Long placeId, Set<Long> imagesId);
    Mono<Long> unlinkImages(Long placeId);
    Mono<Void> deleteAll();
    Mono<Void> deleteById(Long id);

}
