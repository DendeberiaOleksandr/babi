package org.babi.backend.common.dao;

import java.util.Map;

public interface Criteria {

    Map<String, Object> mapCriteriaToQueryArgs(StringBuilder sql);
    Long getPage();
    Long getSize();
    void setPage(Long page);
    void setSize(Long size);

}
