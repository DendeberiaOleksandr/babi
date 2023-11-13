package org.babi.backend.place.api;

import org.babi.backend.common.dao.PageableResponse;
import org.babi.backend.place.dao.PlaceCriteria;
import org.babi.backend.place.domain.Place;
import org.babi.backend.place.service.PlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/places")
public class PlaceController {

    private final PlaceService placeService;
    private final List<SavePlaceApiPreProcessor> savePlaceApiPreProcessors;

    @Autowired
    public PlaceController(PlaceService placeService,
                           List<SavePlaceApiPreProcessor> savePlaceApiPreProcessors) {
        this.placeService = placeService;
        this.savePlaceApiPreProcessors = savePlaceApiPreProcessors;
    }

    @Secured("isAuthenticated()")
    @GetMapping
    public Mono<ResponseEntity<PageableResponse<Place>>> search(PlaceCriteria placeCriteria) {
        return placeService.search(placeCriteria).map(ResponseEntity::ok);
    }

    @Secured("isAuthenticated()")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Place>> findById(@PathVariable Long id) {
        return placeService.findById(id).map(ResponseEntity::ok);
    }

    @Secured("isAuthenticated()")
    @PostMapping
    public Mono<ResponseEntity<Long>> save(@RequestBody Place place, Authentication authentication) {
        savePlaceApiPreProcessors.forEach(savePlaceApiPreProcessor -> savePlaceApiPreProcessor.preProcess(place, authentication));
        return placeService.save(place).map(ResponseEntity::ok);
    }

    @Secured("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Long>> update(@PathVariable Long placeId, @RequestBody Place place, Authentication authentication) {
        return placeService.update(place).map(ResponseEntity::ok);
    }

    @Secured("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Mono<Void> deleteById(@PathVariable Long id) {
        return placeService.deleteById(id);
    }

}
