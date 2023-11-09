package org.babi.backend.place.dao;

import lombok.Getter;
import lombok.Setter;
import org.babi.backend.common.dao.AbstractCriteria;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class PlaceCriteria extends AbstractCriteria {
    private Long placeId;
    private Long categoryId;

    public PlaceCriteria() {
    }

    public PlaceCriteria(Long page, Long size, Long placeId, Long categoryId) {
        super(page, size);
        this.placeId = placeId;
        this.categoryId = categoryId;
    }

    public PlaceCriteria(Long placeId) {
        this.placeId = placeId;
    }

    public PlaceCriteria(Long placeId, Long categoryId) {
        this.placeId = placeId;
        this.categoryId = categoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PlaceCriteria that = (PlaceCriteria) o;
        return Objects.equals(placeId, that.placeId) && Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), placeId, categoryId);
    }

    @Override
    public Map<String, Object> mapCriteriaToQueryArgs(StringBuilder sql) {
        Map<String, Object> args = new HashMap<>();
        boolean whereClauseAdded = false;

        if (placeId != null) {
            appendWhereOrAndClause(whereClauseAdded, sql);
            whereClauseAdded = true;
            sql.append("p.id = :id");
            args.put("id", placeId);
        }

        if (categoryId != null) {
            appendWhereOrAndClause(whereClauseAdded, sql);
            whereClauseAdded = true;
            sql.append("pc.category_id = :categoryId");
            args.put("categoryId", categoryId);
        }

        mapOffsetLimitCriteriaToQueryArgs(args, sql);

        return args;
    }
}
