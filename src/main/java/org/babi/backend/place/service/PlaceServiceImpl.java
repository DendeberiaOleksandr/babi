package org.babi.backend.place.service;

import org.babi.backend.common.dao.PageableResponse;
import org.babi.backend.place.dao.PlaceCriteria;
import org.babi.backend.place.dao.PlaceRepository;
import org.babi.backend.place.domain.Place;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository placeRepository;

    @Autowired
    public PlaceServiceImpl(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    @Override
    public Flux<Place> findAll() {
        return placeRepository.findAll();
    }

    @Override
    public Mono<PageableResponse<Place>> search(PlaceCriteria placeCriteria) {
        return placeRepository.search(placeCriteria);
    }

    @Override
    public Mono<Long> count(PlaceCriteria placeCriteria) {
        return placeRepository.count(placeCriteria);
    }

    @Override
    public Mono<Place> findById(Long id) {
        return placeRepository.findById(id);
    }

    @Override
    public Mono<Long> save(Place place) {
        return placeRepository.save(place).map(Place::getId);
    }

    @Override
    public Mono<Long> update(Place place) {
        return placeRepository.update(place.getId(), place).map(Place::getId);
    }

    @Override
    public Mono<Void> deleteAll() {
        return placeRepository.deleteAll();
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return placeRepository.delete(id);
    }
}
