package org.babi.backend.image.api;

import org.babi.backend.image.domain.Image;
import org.babi.backend.image.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/images")
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @Secured("isAuthenticated()")
    @PostMapping
    public Mono<ResponseEntity<?>> save(@RequestPart("file") Mono<FilePart> multipartFile) {
        return multipartFile.flatMap(filePart -> DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> {
                    try {
                        return dataBuffer.asInputStream().readAllBytes();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(bytes -> imageService.save(new Image(null, bytes, LocalDateTime.now()))))
                .map(ResponseEntity::ok);
    }

    @Secured("isAuthenticated()")
    @GetMapping(value = "/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<DataBuffer> getImage(@PathVariable Long id) {
        return imageService.findById(id).map(image -> DefaultDataBufferFactory.sharedInstance.wrap(image.getContent()));
    }

}
