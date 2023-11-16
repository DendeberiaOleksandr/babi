package org.babi.backend.category.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.babi.backend.common.dao.AbstractCriteria;

import java.util.Map;
import java.util.Set;

@Data
@Builder
public class CategoryCriteria extends AbstractCriteria {

    private Set<? extends Long> ids;
    private Long id;

    @Override
    public Map<String, Object> mapCriteriaToQueryArgs(StringBuilder sql) {
        return null;
    }
}
