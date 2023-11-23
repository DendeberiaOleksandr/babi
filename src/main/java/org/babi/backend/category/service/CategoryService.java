package org.babi.backend.category.service;

import org.babi.backend.category.domain.Category;
import org.babi.backend.common.cache.Cache;
import org.babi.backend.common.dao.ReactiveRepository;
import org.babi.backend.common.service.AbstractService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class CategoryService extends AbstractService<Long, Category> {

    public CategoryService(Cache<Long, Category> cache, ReactiveRepository<Long, Category> repository, ApplicationEventPublisher applicationEventPublisher) {
        super(cache, repository, applicationEventPublisher);
    }
}
