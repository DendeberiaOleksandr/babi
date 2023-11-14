package org.babi.backend.place.dao;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.babi.backend.common.dao.AbstractCriteria;
import org.babi.backend.place.domain.PlaceState;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Builder
public class PlaceCriteria extends AbstractCriteria {
    private Long placeId;
    private Long categoryId;
    private PlaceState placeState;
    private LocalDateTime addingDateFrom;
    private LocalDateTime addingDateTo;
    private String route;
    private String locality;
    private String administrativeAreaLevel2;
    private String administrativeAreaLevel1;
    private String country;
    private String postalCode;

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

        if (placeState != null) {
            appendWhereOrAndClause(whereClauseAdded, sql);
            whereClauseAdded = true;
            sql.append("p.place_state = :placeState");
            args.put("placeState", placeState.name());
        }

        if (addingDateFrom != null) {
            appendWhereOrAndClause(whereClauseAdded, sql);
            whereClauseAdded = true;
            sql.append("p.adding_date >= :addingDateFrom");
            args.put("addingDateFrom", addingDateFrom);
        }

        if (addingDateTo != null) {
            appendWhereOrAndClause(whereClauseAdded, sql);
            whereClauseAdded = true;
            sql.append("p.adding_date <= :addingDateTo");
            args.put("addingDateTo", addingDateFrom);
        }

        if (StringUtils.hasText(route)) {
            appendWhereOrAndClause(whereClauseAdded, sql);
            whereClauseAdded = true;
            sql.append("p.route = :route");
            args.put("route", route);
        }

        if (StringUtils.hasText(locality)) {
            appendWhereOrAndClause(whereClauseAdded, sql);
            whereClauseAdded = true;
            sql.append("p.locality = :locality");
            args.put("locality", locality);
        }

        if (StringUtils.hasText(administrativeAreaLevel2)) {
            appendWhereOrAndClause(whereClauseAdded, sql);
            whereClauseAdded = true;
            sql.append("p.administrative_area_level_2 = :administrativeAreaLevel2");
            args.put("administrativeAreaLevel2", administrativeAreaLevel2);
        }

        if (StringUtils.hasText(administrativeAreaLevel1)) {
            appendWhereOrAndClause(whereClauseAdded, sql);
            whereClauseAdded = true;
            sql.append("p.administrative_area_level_1 = :administrativeAreaLevel1");
            args.put("administrativeAreaLevel1", administrativeAreaLevel1);
        }

        if (StringUtils.hasText(country)) {
            appendWhereOrAndClause(whereClauseAdded, sql);
            whereClauseAdded = true;
            sql.append("p.country = :country");
            args.put("country", country);
        }

        if (StringUtils.hasText(postalCode)) {
            appendWhereOrAndClause(whereClauseAdded, sql);
            whereClauseAdded = true;
            sql.append("p.postalCode = :postalCode");
            args.put("postalCode", postalCode);
        }

        mapOffsetLimitCriteriaToQueryArgs(args, sql);

        return args;
    }
}
