package org.babi.backend.image.service;

import lombok.extern.slf4j.Slf4j;
import org.babi.backend.image.dao.ImageRepository;
import org.babi.backend.image.domain.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;

    @Autowired
    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public Mono<Long> save(Image image) {
        return imageRepository.save(image).map(Image::getId);
    }

    @Override
    public Mono<Image> findById(Long id) {
        return imageRepository.findById(id);
    }
}
