package org.babi.backend.place.dao;

import org.babi.backend.category.dao.CategoryRepository;
import org.babi.backend.category.domain.Category;
import org.babi.backend.common.exception.ResourceNotFoundException;
import org.babi.backend.dao.AbstractDaoITTest;
import org.babi.backend.image.dao.ImageRepository;
import org.babi.backend.image.domain.Image;
import org.babi.backend.place.domain.Address;
import org.babi.backend.place.domain.Place;
import org.babi.backend.place.domain.PlaceState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.r2dbc.core.DatabaseClient;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PlaceRepositoryImplTest extends AbstractDaoITTest {

    private PlaceRepositoryImpl placeRepository;
    private ImageRepository imageRepository;
    private CategoryRepository categoryRepository;

    @Autowired
    public PlaceRepositoryImplTest(DatabaseClient databaseClient, ImageRepository imageRepository, CategoryRepository categoryRepository) {
        this.placeRepository = new PlaceRepositoryImpl(databaseClient);
        this.imageRepository = imageRepository;
        this.categoryRepository = categoryRepository;
    }

    @BeforeEach
    public void clean(){
        placeRepository.deleteAll().block();
        categoryRepository.deleteAll().block();
        imageRepository.deleteAll().block();
    }

    @Test
    void save_whenNestedEntitiesDoNotExist_thenThrowException() {
        // given

        // when
        assertThrows(DataIntegrityViolationException.class,
                () -> placeRepository.save(new Place(null, "name", Set.of(1L), Set.of(1L),
                        null, LocalDateTime.now(), "link", PlaceState.APPROVED,
                        new Address("street", "street", "locality", "adminLevel2",
                                "adminLevel1", "country", "postalCode", 0.0, 0.0))).block());

        // then
    }

    @Test
    void save_whenEntityHasValidState_thenShouldBeSaved() {
        // given
        Image image = imageRepository.save(new Image(null, new byte[]{}, LocalDateTime.now())).block();
        Category category = categoryRepository.save(new Category(null, "name")).block();

        // when
        Place place = placeRepository.save(new Place(null, "name", Set.of(image.getId()), Set.of(category.getId()), null, LocalDateTime.now(), "link", PlaceState.APPROVED,
                new Address("street", "street", "locality", "adminLevel2",
                        "adminLevel1", "country", "postalCode", 0.0, 0.0))).block();

        // then
        assertNotNull(place);
        assertNotNull(place.getId());
    }

    @Test
    void findAll_whenThereAreNoPlaces_thenReturnEmptyStream() {
        // given

        // when
        List<Place> result = placeRepository.findAll().collectList().block();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_whenThereArePlaces_thenReturnThem() {
        // given
        Image image = imageRepository.save(new Image(null, new byte[]{}, LocalDateTime.now())).block();
        Category category = categoryRepository.save(new Category(null, "name")).block();
        placeRepository.save(new Place(null, "name", Set.of(image.getId()), Set.of(category.getId()), null, LocalDateTime.now(), "link", PlaceState.APPROVED,
                new Address("street", "street", "locality", "adminLevel2",
                        "adminLevel1", "country", "postalCode", 0.0, 0.0))).block();

        // when
        List<Place> result = placeRepository.findAll().collectList().block();

        // then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void findById_whenThereIsNoPlaceWithMatchingId_thenThrowException() {
        // given

        // when
        assertThrows(ResourceNotFoundException.class, () -> placeRepository.findById(1L).block());

        // then
    }

    @Test
    void findById_whenThereIsPlaceWithMatchingId_thenReturnIt() {
        // given
        Image image = imageRepository.save(new Image(null, new byte[]{}, LocalDateTime.now())).block();
        Category category = categoryRepository.save(new Category(null, "name")).block();
        Place place = placeRepository.save(new Place(null, "name", Set.of(image.getId()), Set.of(category.getId()), null, LocalDateTime.now(), "link", PlaceState.APPROVED,
                new Address("street", "street", "locality", "adminLevel2",
                        "adminLevel1", "country", "postalCode", 0.0, 0.0))).block();

        // when
        Place result = placeRepository.findById(place.getId()).block();

        // then
        assertNotNull(result);
    }

    @Test
    void search_whenCategoryIdCriteriaProvidedAndThereIsNoMatchingData_thenReturnEmptyList() {
        // given
        Image image = imageRepository.save(new Image(null, new byte[]{}, LocalDateTime.now())).block();
        Category category = categoryRepository.save(new Category(null, "name")).block();
        Place place = placeRepository.save(new Place(null, "name", Set.of(image.getId()), Set.of(category.getId()), null, LocalDateTime.now(), "link", PlaceState.APPROVED,
                new Address("street", "street", "locality", "adminLevel2",
                        "adminLevel1", "country", "postalCode", 0.0, 0.0))).block();
        PlaceCriteria placeCriteria = PlaceCriteria.builder().build();
        placeCriteria.setCategoryId(category.getId() + 1);

        // when
        List<Place> data = placeRepository.search(placeCriteria).block().getData();

        // then
        assertNotNull(data);
        assertTrue(data.isEmpty());
    }

    @Test
    void search_whenCategoryIdCriteriaProvidedAndThereIsMatchingData_thenReturnData() {
        // given
        Image image = imageRepository.save(new Image(null, new byte[]{}, LocalDateTime.now())).block();
        Category category = categoryRepository.save(new Category(null, "name")).block();
        placeRepository.save(new Place(null, "name", Set.of(image.getId()), Set.of(category.getId()), null, LocalDateTime.now(), "link", PlaceState.APPROVED,
                new Address("street", "street", "locality", "adminLevel2",
                        "adminLevel1", "country", "postalCode", 0.0, 0.0))).block();
        PlaceCriteria placeCriteria = PlaceCriteria.builder().build();
        placeCriteria.setCategoryId(category.getId());

        // when
        List<Place> data = placeRepository.search(placeCriteria).block().getData();

        // then
        assertNotNull(data);
        assertFalse(data.isEmpty());
    }

    @Test
    void update_whenAllFieldsAreChanged_thenShouldBeChanged() {
        // given
        String name = "name";
        String pageLink = "pageLink";
        double longitude = 0.0;
        double latitude = 0.0;
        PlaceState placeState = PlaceState.REVIEW;

        Image image = imageRepository.save(new Image(null, new byte[]{}, LocalDateTime.now())).block();
        Set<Long> imagesId = new HashSet<>();
        imagesId.add(image.getId());

        Category category = categoryRepository.save(new Category(null, "name")).block();
        Set<Long> categoriesId = new HashSet<>();
        categoriesId.add(category.getId());

        Place place = placeRepository.save(new Place(null, name, imagesId, categoriesId, null, LocalDateTime.now(), pageLink, placeState,
                new Address("street", "street", "locality", "adminLevel2",
                        "adminLevel1", "country", "postalCode", longitude, latitude))).block();
        place.setName("anotherName");
        place.setPageLink("anotherPageLink");
        Address address = place.getAddress();
        address.setLongitude(10.0);
        address.setLatitude(10.0);
        place.setAddress(address);
        place.setPlaceState(placeState);

        Image image2 = imageRepository.save(new Image(null, new byte[]{}, LocalDateTime.now())).block();
        place.getImagesId().add(image2.getId());

        Category category2 = categoryRepository.save(new Category(null, "name2")).block();
        place.getCategoriesId().add(category2.getId());

        // when
        placeRepository.update(place).block();
        Place updatedPlace = placeRepository.findAll().blockFirst();

        // then
        assertNotNull(updatedPlace);
        assertNotEquals(name, updatedPlace.getName());
        assertNotEquals(pageLink, updatedPlace.getPageLink());
        Address updatedAddress = updatedPlace.getAddress();
        assertNotEquals(longitude, updatedAddress.getLongitude());
        assertNotEquals(latitude, updatedAddress.getLatitude());
        assertNotEquals(placeState, updatedPlace.getPlaceState());
        assertNotEquals(imagesId, updatedPlace.getImagesId());
        assertNotEquals(categoriesId, updatedPlace.getCategoriesId());
    }

    @Test
    void deleteAll_whenThereArePlaces_thenShouldBeDeleted() {
        // given
        Image image = imageRepository.save(new Image(null, new byte[]{}, LocalDateTime.now())).block();
        Category category = categoryRepository.save(new Category(null, "name")).block();
        placeRepository.save(new Place(null, "name", Set.of(image.getId()), Set.of(category.getId()), null, LocalDateTime.now(), "link", PlaceState.APPROVED,
                new Address("street", "street", "locality", "adminLevel2",
                        "adminLevel1", "country", "postalCode", 0.0, 0.0))).block();

        // when
        placeRepository.deleteAll().block();
        List<Place> result = placeRepository.findAll().collectList().block();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteById_whenThereIsMatchingIdPlace_thenShouldBeDeleted() {
        // given
        Image image = imageRepository.save(new Image(null, new byte[]{}, LocalDateTime.now())).block();
        Category category = categoryRepository.save(new Category(null, "name")).block();
        Place place = placeRepository.save(new Place(null, "name", Set.of(image.getId()), Set.of(category.getId()), null, LocalDateTime.now(), "link", PlaceState.APPROVED,
                new Address("street", "street", "locality", "adminLevel2",
                        "adminLevel1", "country", "postalCode", 0.0, 0.0))).block();

        // when
        placeRepository.deleteById(place.getId()).block();
        List<Place> result = placeRepository.findAll().collectList().block();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}