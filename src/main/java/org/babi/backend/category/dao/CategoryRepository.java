package org.babi.backend.category.dao;

import org.babi.backend.category.domain.Category;
import org.babi.backend.common.dao.ReactiveRepository;

public interface CategoryRepository extends ReactiveRepository<Long, Category> {

}
