package org.babi.backend.place.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.babi.backend.category.domain.Category;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = { "categories" })
@EqualsAndHashCode(exclude = { "categories" })
@Builder
public class Place {

    @Id
    private Long id;
    private String name;
    private Set<Long> imagesId;
    private Set<Long> categoriesId;
    private List<Category> categories;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime addingDate;
    private String pageLink;
    private PlaceState placeState;
    private Address address;

}
