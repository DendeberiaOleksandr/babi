package org.babi.backend.category.dao;

import lombok.Builder;
import lombok.Data;
import org.babi.backend.common.dao.AbstractCriteria;
import org.babi.backend.common.dao.DaoUtil;
import org.springframework.data.util.Pair;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class CategoryCriteria extends AbstractCriteria {

    private Set<? extends Long> ids;
    private Long id;

    @Override
    public Map<String, Object> mapCriteriaToQueryArgs(StringBuilder sql) {
        Map<String, Object> args = new HashMap<>();
        boolean whereClauseAdded = false;

        if (id != null) {
            appendWhereOrAndClause(whereClauseAdded, sql);
            whereClauseAdded = true;
            sql.append("id = :id");
            args.put("id", id);
        }

        if (!CollectionUtils.isEmpty(ids)) {
            appendWhereOrAndClause(whereClauseAdded, sql);
            whereClauseAdded = true;
            Pair<String, Map<String, Object>> sqlArgs = DaoUtil.buildInStatementTuple(ids, "categoryId");
            sql.append("id in ").append(sqlArgs.getFirst());
            args.putAll(sqlArgs.getSecond());
        }

        return args;
    }
}
