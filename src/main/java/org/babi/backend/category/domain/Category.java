package org.babi.backend.category.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.babi.backend.category.domain.event.CategoryChangedEvent;
import org.babi.backend.category.domain.event.CategoryRemovedEvent;
import org.babi.backend.category.domain.event.CategorySavedEvent;
import org.babi.backend.common.domain.Entity;
import org.babi.backend.common.domain.event.EntityChangedEvent;
import org.babi.backend.common.domain.event.EntityRemovedEvent;
import org.babi.backend.common.domain.event.EntitySavedEvent;
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
public class Category implements Entity<Long> {

    @Id
    private Long id;
    private String name;

    @Override
    public Class<? extends EntityRemovedEvent> getEntityRemovedEventClass() {
        return CategoryRemovedEvent.class;
    }

    @Override
    public Class<? extends EntitySavedEvent> getEntitySavedEventClass() {
        return CategorySavedEvent.class;
    }

    @Override
    public Class<? extends EntityChangedEvent> getEntityChangedEventClass() {
        return CategoryChangedEvent.class;
    }
}
