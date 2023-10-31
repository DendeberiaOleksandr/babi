package org.babi.backend.category.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@AllArgsConstructor
@NoArgsConstructor
@Table("category")
@Setter
@Getter
@EqualsAndHashCode
@ToString
@Builder
public class Category {

    @Id
    private Long id;
    private String name;

}
