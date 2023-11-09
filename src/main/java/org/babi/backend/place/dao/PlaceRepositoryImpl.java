package org.babi.backend.place.dao;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.babi.backend.category.domain.Category;
import org.babi.backend.common.dao.DaoUtil;
import org.babi.backend.common.dao.PageableResponse;
import org.babi.backend.common.exception.ResourceNotFoundException;
import org.babi.backend.place.domain.Place;
import org.babi.backend.place.domain.PlaceState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class PlaceRepositoryImpl implements PlaceRepository {

    private final DatabaseClient databaseClient;

    @Autowired
    public PlaceRepositoryImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<PageableResponse<Place>> search(PlaceCriteria placeCriteria) {
        return findAll(placeCriteria)
                .collectList()
                .flatMap(places -> count(placeCriteria).map(count -> new PageableResponse<>(places, count)));
    }

    @Override
    public Flux<Place> findAll() {
        return findAll(null);
    }

    @Override
    public Mono<Long> count(PlaceCriteria placeCriteria) {
        final StringBuilder sql = new StringBuilder("select count(*) " +
                "from place p " +
                "join place_category pc " +
                "on p.id = pc.place_id " +
                "join category c " +
                "on pc.category_id = c.id " +
                "join place_image pi2 " +
                "on p.id = pi2.place_id");
        Map<String, Object> args = new HashMap<>();
        if (placeCriteria != null) {
            args = placeCriteria.mapCriteriaToQueryArgs(sql);
        }
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql.toString());

        for (Map.Entry<String, Object> arg : args.entrySet()) {
            executeSpec = executeSpec.bind(arg.getKey(), arg.getValue());
        }
        return executeSpec
                .map((row, rowMetadata) -> row.get(0, Long.class))
                .first();
    }

    @Override
    public Mono<Place> findById(Long id) {
        return findAll(new PlaceCriteria(id)).switchIfEmpty(Mono.error(new ResourceNotFoundException(Place.class, "id", id))).single();
    }

    private Flux<Place> findAll(PlaceCriteria placeCriteria) {
        final StringBuilder sql = new StringBuilder("select p.id, p.name, p.adding_date, p.page_link, p.longitude, p.latitude, p.place_state, " +
                "c.id as category_id, c.name as category_name, " +
                "pi2.image_id " +
                "from place p " +
                "join place_category pc " +
                "on p.id = pc.place_id " +
                "join category c " +
                "on pc.category_id = c.id " +
                "join place_image pi2 " +
                "on p.id = pi2.place_id");
        Map<String, Object> args = new HashMap<>();
        if (placeCriteria != null) {
            args = placeCriteria.mapCriteriaToQueryArgs(sql);
        }
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql.toString());

        for (Map.Entry<String, Object> arg : args.entrySet()) {
            executeSpec = executeSpec.bind(arg.getKey(), arg.getValue());
        }
        return executeSpec
                .map((row, rowMetadata) -> new PlaceCategoryImageRow(
                        row.get("id", Long.class),
                        row.get("name", String.class),
                        row.get("adding_date", LocalDateTime.class),
                        row.get("page_link", String.class),
                        Optional.ofNullable(row.get("longitude", Double.class)).orElse(0.0),
                        Optional.ofNullable(row.get("latitude", Double.class)).orElse(0.0),
                        row.get("category_id", Long.class),
                        row.get("category_name", String.class),
                        row.get("image_id", Long.class),
                        PlaceState.valueOf(row.get("place_state", String.class))
                ))
                .all()
                .collectList()
                .map(placeCategoryImageRows -> placeCategoryImageRows.stream().collect(Collectors.groupingBy(PlaceCategoryImageRow::getId)))
                .flatMapMany(places -> Flux.fromStream(places.values().stream().map(placesRows -> {
                    Place place = new Place();
                    List<Category> categories = new ArrayList<>();
                    Set<Long> categoriesId = new HashSet<>();
                    Set<Long> imagesId = new HashSet<>();
                    placesRows.forEach(placeRow -> {
                        place.setId(placeRow.getId());
                        place.setName(placeRow.getName());
                        place.setAddingDate(placeRow.getAddingDate());
                        place.setLatitude(placeRow.getLatitude());
                        place.setLongitude(placeRow.getLongitude());
                        place.setPageLink(placeRow.getPageLink());
                        Long categoryId = placeRow.getCategoryId();
                        categories.add(new Category(categoryId, placeRow.getCategoryName()));
                        categoriesId.add(categoryId);
                        imagesId.add(placeRow.getImageId());
                    });
                    place.setCategories(categories);
                    place.setCategoriesId(categoriesId);
                    place.setImagesId(imagesId);
                    return place;
                })));
    }

    @Override
    public Mono<Place> save(Place place) {
        return databaseClient.sql("insert into place(name, adding_date, page_link, longitude, latitude, place_state) values (:name, :addingDate, :pageLink, :longitude, :latitude, :placeState)")
                .bind("name", place.getName())
                .bind("addingDate", place.getAddingDate())
                .bind("pageLink", place.getPageLink())
                .bind("longitude", place.getLongitude())
                .bind("latitude", place.getLatitude())
                .bind("placeState", place.getPlaceState().name())
                .filter((statement, executeFunction) -> statement.returnGeneratedValues("id").execute())
                .fetch().first()
                .doOnNext(result -> place.setId(Long.parseLong(result.get("id").toString())))
                .flatMap(result -> linkCategories(place.getId(), place.getCategoriesId()))
                .flatMap(placeId -> linkImages(placeId, place.getImagesId()))
                .thenReturn(place);
    }

    @Override
    public Mono<Place> update(Place place) {
        return databaseClient.sql("update place set name = :name, page_link = :pageLink, longitude = :longitude," +
                "latitude = :latitude, place_state = :placeState where id = :id")
                .bind("name", place.getName())
                .bind("pageLink", place.getPageLink())
                .bind("longitude", place.getLongitude())
                .bind("latitude", place.getLatitude())
                .bind("placeState", place.getPlaceState().name())
                .bind("id", place.getId())
                .fetch()
                .all()
                .flatMap(result -> unlinkCategories(place.getId()))
                .flatMap(placeId -> linkCategories(place.getId(), place.getCategoriesId()))
                .flatMap(this::unlinkImages)
                .flatMap(placeId -> linkImages(placeId, place.getImagesId()))
                .then(Mono.just(place));
    }

    @Override
    public Mono<Long> linkCategories(Long placeId, Set<Long> categoriesId) {
        if (CollectionUtils.isEmpty(categoriesId)) {
            return Mono.just(placeId);
        }
        String sql = buildLinkCategoriesSQL(placeId, categoriesId);
        return databaseClient.sql(sql)
                .fetch()
                .all()
                .then(Mono.just(placeId));
    }

    private String buildLinkCategoriesSQL(Long placeId, Set<Long> categoriesId) {
        return DaoUtil.buildLinkQuery("place_category", placeId, categoriesId, "place_id", "category_id");
    }

    @Override
    public Mono<Long> unlinkCategories(Long placeId, Set<Long> categoriesId) {
        if (CollectionUtils.isEmpty(categoriesId)) {
            return Mono.just(placeId);
        }
        return databaseClient.sql(buildUnlinkCategories(categoriesId))
                .bind("placeId", placeId)
                .fetch()
                .all()
                .then(Mono.just(placeId));
    }

    @Override
    public Mono<Long> unlinkCategories(Long placeId) {
        return databaseClient.sql("delete from place_category where place_id = :id")
                .bind("id", placeId)
                .fetch()
                .all()
                .then(Mono.just(placeId));
    }

    private String buildUnlinkCategories(Set<Long> categoriesId) {
        return DaoUtil.buildUnlinkQuery("place_category", categoriesId, "place_id", "placeId", "category_id");
    }

    @Override
    public Mono<Long> linkImages(Long placeId, Set<Long> imagesId) {
        if (CollectionUtils.isEmpty(imagesId)) {
            return Mono.just(placeId);
        }
        String sql = buildLinkImagesSQL(placeId, imagesId);
        return databaseClient.sql(sql)
                .fetch()
                .all()
                .then(Mono.just(placeId));
    }

    private String buildLinkImagesSQL(Long placeId, Set<Long> imagesId) {
        return DaoUtil.buildLinkQuery("place_image", placeId, imagesId, "place_id", "image_id");
    }

    @Override
    public Mono<Long> unlinkImages(Long placeId, Set<Long> imagesId) {
        if (CollectionUtils.isEmpty(imagesId)) {
            return Mono.just(placeId);
        }
        return databaseClient.sql(buildUnlinkImagesSQL(imagesId))
                .bind("placeId", placeId)
                .fetch()
                .all()
                .then(Mono.just(placeId));
    }

    @Override
    public Mono<Long> unlinkImages(Long placeId) {
        return databaseClient.sql("delete from place_image where place_id = :id")
                .bind("id", placeId)
                .fetch()
                .all()
                .then(Mono.just(placeId));
    }

    @Override
    public Mono<Void> deleteAll() {
        return databaseClient.sql("delete from place_category")
                .flatMap(result -> databaseClient.sql("delete from place_image").then())
                .flatMap(unused -> databaseClient.sql("delete from place").then())
                .then();
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return databaseClient.sql("delete from place_category where place_id = :placeId")
                .bind("placeId", id)
                .flatMap(result -> databaseClient.sql("delete from place_image where place_id = :placeId").bind("placeId", id).then())
                .flatMap(unused -> databaseClient.sql("delete from place where id = :id").bind("id", id).then())
                .then();
    }

    private String buildUnlinkImagesSQL(Set<Long> imagesId) {
        return DaoUtil.buildUnlinkQuery("place_image", imagesId, "place_id", "placeId", "image_id");
    }

    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode
    @ToString
    private static class PlaceCategoryImageRow {
        private Long id;
        private String name;
        private LocalDateTime addingDate;
        private String pageLink;
        private double longitude;
        private double latitude;
        private Long categoryId;
        private String categoryName;
        private Long imageId;
        private PlaceState placeState;
    }
}
