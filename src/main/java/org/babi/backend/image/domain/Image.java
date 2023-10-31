package org.babi.backend.image.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = { "content" })
@EqualsAndHashCode
@Table("image")
@Builder
public class Image {

    @Id
    private Long id;
    private byte[] content;
    private LocalDateTime createdAt;
}
