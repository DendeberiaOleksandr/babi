package org.babi.backend.image.service;

import org.babi.backend.image.domain.Image;
import reactor.core.publisher.Mono;

public interface ImageService  {

    Mono<Long> save(Image image);
    Mono<Image> findById(Long id);

}
