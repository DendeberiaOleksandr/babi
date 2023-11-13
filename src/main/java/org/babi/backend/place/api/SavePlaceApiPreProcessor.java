package org.babi.backend.place.api;

import org.babi.backend.place.domain.Place;
import org.springframework.security.core.Authentication;

public interface SavePlaceApiPreProcessor {
    void preProcess(Place place, Authentication authentication);
}
