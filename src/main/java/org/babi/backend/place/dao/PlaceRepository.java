package org.babi.backend.place.dao;

import org.babi.backend.common.dao.ReactivePageableRepository;
import org.babi.backend.common.dao.ReactiveRepository;
import org.babi.backend.place.domain.Place;

public interface PlaceRepository extends ReactiveRepository<Long, Place>, ReactivePageableRepository<Long, Place> {

}
