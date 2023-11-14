package org.babi.backend.common.dao;

import java.util.Map;

public abstract class AbstractCriteria implements Criteria {

    private Long page;
    private Long size;

    public AbstractCriteria() {
    }

    public AbstractCriteria(Long page, Long size) {
        this.page = page;
        this.size = size;
    }

    protected void mapOffsetLimitCriteriaToQueryArgs(Map<String, Object> args, StringBuilder sql) {
        if (page != null && size != null) {
            long offset = page == 0 ? size : page * size;
            sql.append(" offset :offset ");
            args.put("offset", offset);
            sql.append(" limit :limit ");
            args.put("limit", size);
        }
    }

    protected void appendWhereOrAndClause(boolean whereClauseAdded, StringBuilder sql) {
        sql.append(
                whereClauseAdded ? " and " : " where "
        );
    }

    @Override
    public Long getPage() {
        return page;
    }

    @Override
    public Long getSize() {
        return size;
    }

    @Override
    public void setPage(Long page) {
        this.page = page;
    }

    @Override
    public void setSize(Long size) {
        this.size = size;
    }
}
