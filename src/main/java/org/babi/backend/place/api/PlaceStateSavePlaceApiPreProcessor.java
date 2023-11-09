package org.babi.backend.place.api;

import org.babi.backend.place.domain.Place;
import org.babi.backend.place.domain.PlaceState;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class PlaceStateSavePlaceApiPreProcessor implements SavePlaceApiPreProcessor {
    @Override
    public void preProcess(Place place, Authentication authentication) {
        PlaceState placeState = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ADMIN")) ? PlaceState.APPROVED : PlaceState.REVIEW;
        place.setPlaceState(placeState);
    }
}
