package org.babi.backend.place.dao;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.babi.backend.category.domain.Category;
import org.babi.backend.common.dao.AbstractRepository;
import org.babi.backend.common.dao.Criteria;
import org.babi.backend.common.dao.PageableResponse;
import org.babi.backend.common.exception.ResourceNotFoundException;
import org.babi.backend.place.domain.Address;
import org.babi.backend.place.domain.Place;
import org.babi.backend.place.domain.PlaceState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.testcontainers.shaded.com.google.common.annotations.VisibleForTesting;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class PlaceRepositoryImpl extends AbstractRepository<Long, Place> implements PlaceRepository {

    @Autowired
    public PlaceRepositoryImpl(DatabaseClient databaseClient) {
        super(databaseClient);
    }

    @Override
    public Mono<PageableResponse<Place>> search(Criteria criteria) {
        return findAll(criteria)
                .collectList()
                .flatMap(places -> count(criteria).map(count -> new PageableResponse<>(places, count)));
    }

    @Override
    public Flux<Place> findAll() {
        return findAll(null);
    }

    @Override
    public Mono<Long> count(Criteria criteria) {
        final StringBuilder sql = new StringBuilder("select count(distinct p.id) " +
                "from place p " +
                "join place_category pc " +
                "on p.id = pc.place_id " +
                "join category c " +
                "on pc.category_id = c.id " +
                "join place_image pi2 " +
                "on p.id = pi2.place_id");
        return count(sql, criteria);
    }

    @Override
    public Flux<Place> findAllById(Set<? extends Long> id) {
        return null;
    }

    @Override
    public Mono<Place> findById(Long id) {
        return findAll(PlaceCriteria.builder().placeId(id).build()).switchIfEmpty(Mono.error(new ResourceNotFoundException(Place.class, "id", id))).single();
    }

    private Flux<Place> findAll(Criteria criteria) {
        final StringBuilder sql = new StringBuilder("select p.id, p.name, p.adding_date, p.page_link, p.longitude, p.latitude, p.place_state, " +
                "p.street_number, p.route, p.locality, p.administrative_area_level_2, p.administrative_area_level_1, p.country, p.postal_code, " +
                "c.id as category_id, c.name as category_name, " +
                "pi2.image_id " +
                "from place p " +
                "join place_category pc " +
                "on p.id = pc.place_id " +
                "join category c " +
                "on pc.category_id = c.id " +
                "join place_image pi2 " +
                "on p.id = pi2.place_id");

        DatabaseClient.GenericExecuteSpec executeSpec = executeSpecFilledByArgs(sql, criteria);

        return executeSpec
                .map((row, rowMetadata) -> new PlaceCategoryImageRow(
                        row.get("id", Long.class),
                        row.get("name", String.class),
                        row.get("adding_date", LocalDateTime.class),
                        row.get("page_link", String.class),
                        row.get("category_id", Long.class),
                        row.get("category_name", String.class),
                        row.get("image_id", Long.class),
                        PlaceState.valueOf(row.get("place_state", String.class)),
                        new Address(
                                row.get("street_number", String.class),
                                row.get("route", String.class),
                                row.get("locality", String.class),
                                row.get("administrative_area_level_2", String.class),
                                row.get("administrative_area_level_1", String.class),
                                row.get("country", String.class),
                                row.get("postal_code", String.class),
                                Optional.ofNullable(row.get("longitude", Double.class)).orElse(0.0),
                                Optional.ofNullable(row.get("latitude", Double.class)).orElse(0.0)
                        )
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
                        place.setAddress(placeRow.getAddress());
                        place.setPageLink(placeRow.getPageLink());
                        place.setPlaceState(placeRow.getPlaceState());
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
        Optional<Address> address = Optional.ofNullable(place.getAddress());
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql("insert into place(name, adding_date, page_link, longitude, latitude," +
                        "place_state, street_number, route, locality, administrative_area_level_2," +
                        "administrative_area_level_1, country, postal_code) " +
                        "values (:name, :addingDate, :pageLink, :longitude, :latitude, :placeState," +
                        ":streetNumber, :route, :locality, :administrativeAreaLevel2, :administrativeAreaLevel1," +
                        ":country, :postalCode)")
                .bind("name", place.getName())
                .bind("addingDate", Optional.ofNullable(place.getAddingDate()).orElse(LocalDateTime.now()))
                .bind("pageLink", place.getPageLink())
                .bind("longitude", address.map(Address::getLongitude).orElse(0.0))
                .bind("latitude", address.map(Address::getLatitude).orElse(0.0))
                .bind("placeState", place.getPlaceState().name());

                return bindNullableParametersForInsertQuery(executeSpec, place)
                .filter((statement, executeFunction) -> statement.returnGeneratedValues("id").execute())
                .fetch().first()
                .doOnNext(result -> place.setId(Long.parseLong(result.get("id").toString())))
                .flatMap(result -> linkCategories(place.getId(), place.getCategoriesId()))
                .flatMap(placeId -> linkImages(placeId, place.getImagesId()))
                .thenReturn(place);
    }

    private DatabaseClient.GenericExecuteSpec bindNullableParametersForInsertQuery(DatabaseClient.GenericExecuteSpec executeSpec, Place place) {
        Address address = place.getAddress();
        if (address == null) {
            return bindNullForAddressComponents(executeSpec);
        } else {
            String streetNumber = address.getStreetNumber();
            executeSpec = streetNumber == null ? executeSpec.bindNull("streetNumber", String.class) : executeSpec.bind("streetNumber", streetNumber);

            String route = address.getRoute();
            executeSpec = route == null ? executeSpec.bindNull("route", String.class) : executeSpec.bind("route", route);

            String locality = address.getLocality();
            executeSpec = locality == null ? executeSpec.bindNull("locality", String.class) : executeSpec.bind("locality", locality);

            String administrativeAreaLevel2 = address.getAdministrativeAreaLevel2();
            executeSpec = administrativeAreaLevel2 == null ? executeSpec.bindNull("administrativeAreaLevel2", String.class) : executeSpec.bind("administrativeAreaLevel2", administrativeAreaLevel2);

            String administrativeAreaLevel1 = address.getAdministrativeAreaLevel1();
            executeSpec = administrativeAreaLevel1 == null ? executeSpec.bindNull("administrativeAreaLevel1", String.class) : executeSpec.bind("administrativeAreaLevel1", administrativeAreaLevel1);

            String country = address.getCountry();
            executeSpec = country == null ? executeSpec.bindNull("country", String.class) : executeSpec.bind("country", country);

            String postalCode = address.getPostalCode();
            return country == null ? executeSpec.bindNull("postalCode", String.class) : executeSpec.bind("postalCode", postalCode);
        }
    }

    private DatabaseClient.GenericExecuteSpec bindNullForAddressComponents(DatabaseClient.GenericExecuteSpec executeSpec) {
        return executeSpec
                .bindNull("streetNumber", String.class)
                .bindNull("route", String.class)
                .bindNull("locality", String.class)
                .bindNull("administrative_area_level_2", String.class)
                .bindNull("administrative_area_level_1", String.class)
                .bindNull("country", String.class)
                .bindNull("postal_code", String.class);
    }

    @Override
    public Mono<Place> update(Long id, Place place) {
        Optional<Address> address = Optional.ofNullable(place.getAddress());
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql("update place set name = :name, page_link = :pageLink, longitude = :longitude," +
                        "latitude = :latitude, place_state = :placeState, street_number = :streetNumber," +
                        "route = :route, locality = :locality, administrative_area_level_2 = :administrativeAreaLevel2," +
                        "administrative_area_level_1 = :administrativeAreaLevel1, country = :country," +
                        "postal_code = :postalCode where id = :id")
                .bind("name", place.getName())
                .bind("pageLink", place.getPageLink())
                .bind("longitude", address.map(Address::getLongitude).orElse(0.0))
                .bind("latitude", address.map(Address::getLatitude).orElse(0.0))
                .bind("placeState", place.getPlaceState().name())
                .bind("id", place.getId());

        return bindNullableParametersForInsertQuery(executeSpec, place)
                .fetch()
                .all()
                .flatMap(result -> unlinkCategories(place.getId()))
                .flatMap(placeId -> linkCategories(place.getId(), place.getCategoriesId()))
                .flatMap(this::unlinkImages)
                .flatMap(placeId -> linkImages(placeId, place.getImagesId()))
                .then(Mono.just(place));
    }

    @VisibleForTesting
    Mono<Long> linkCategories(Long placeId, Set<Long> categoriesId) {
        return linkNestedEntities(PlaceCategoryTable.TABLE, placeId, categoriesId, PlaceCategoryTable.PLACE_ID, PlaceCategoryTable.CATEGORY_ID);
    }

    @VisibleForTesting
    Mono<Long> unlinkCategories(Long placeId, Set<Long> categoriesId) {
        return unlinkNestedEntities(PlaceCategoryTable.TABLE, placeId, categoriesId, PlaceCategoryTable.PLACE_ID, PlaceCategoryTable.CATEGORY_ID);
    }

    @VisibleForTesting
    Mono<Long> unlinkCategories(Long placeId) {
        return unlinkNestedEntities(PlaceCategoryTable.TABLE, PlaceCategoryTable.PLACE_ID, placeId);
    }

    @VisibleForTesting
    Mono<Long> linkImages(Long placeId, Set<Long> imagesId) {
        return linkNestedEntities(PlaceImageTable.TABLE, placeId, imagesId, PlaceImageTable.PLACE_ID, PlaceImageTable.IMAGE_ID);
    }

    @VisibleForTesting
    Mono<Long> unlinkImages(Long placeId, Set<Long> imagesId) {
        return unlinkNestedEntities(PlaceImageTable.TABLE, placeId, imagesId, PlaceImageTable.PLACE_ID, PlaceImageTable.IMAGE_ID);
    }

    @VisibleForTesting
    Mono<Long> unlinkImages(Long placeId) {
        return unlinkNestedEntities(PlaceImageTable.TABLE, PlaceImageTable.PLACE_ID, placeId);
    }

    @Override
    public Mono<Void> deleteAll() {
        return deleteAll(List.of(PlaceCategoryTable.TABLE, PlaceImageTable.TABLE, PlaceTable.TABLE));
    }

    @Override
    public Mono<Void> delete(Long id) {
        return deleteById(List.of(
                new DeleteByIdParam(PlaceImageTable.TABLE, PlaceImageTable.PLACE_ID, id),
                new DeleteByIdParam(PlaceCategoryTable.TABLE, PlaceCategoryTable.PLACE_ID, id),
                new DeleteByIdParam(PlaceTable.TABLE, PlaceTable.ID, id)
        ));
    }

    @Override
    public Mono<Void> delete(Place place) {
        return delete(place.getId());
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
        private Long categoryId;
        private String categoryName;
        private Long imageId;
        private PlaceState placeState;
        private Address address;
    }

    private static class PlaceImageTable {
        private static final String TABLE = "place_image";
        private static final String PLACE_ID = "place_id";
        private static final String IMAGE_ID = "image_id";
    }

    private static class PlaceCategoryTable {
        private static final String TABLE = "place_category";
        private static final String PLACE_ID = "place_id";
        private static final String CATEGORY_ID = "category_id";
    }

    private static class PlaceTable {
        private static final String TABLE = "place";
        private static final String ID = "id";
        private static final String NAME = "name";
        private static final String ADDING_DATE = "adding_date";
        private static final String PAGE_LINK = "page_link";
        private static final String LONGITUDE = "longitude";
        private static final String LATITUDE = "latitude";
        private static final String PLACE_STATE = "place_state";
        private static final String STREET_NUMBER = "street_number";
        private static final String ROUTE = "route";
        private static final String LOCALITY = "locality";
        private static final String ADMINISTRATIVE_AREA_LEVEL_2 = "administrative_area_level_2";
        private static final String ADMINISTRATIVE_AREA_LEVEL_1 = "administrative_area_level_1";
        private static final String COUNTRY = "country";
        private static final String POSTAL_CODE = "postal_code";
    }
}
