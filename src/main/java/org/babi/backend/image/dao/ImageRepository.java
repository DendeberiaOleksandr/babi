package org.babi.backend.image.dao;

import org.babi.backend.image.domain.Image;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends ReactiveCrudRepository<Image, Long>  {
}
