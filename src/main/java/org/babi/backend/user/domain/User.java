package org.babi.backend.user.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("usr")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
@ToString
public class User {

    @Id
    private Long id;
    private String username;
    private String email;
    private boolean emailVerified;
    private boolean isServiceTermsAccepted;
    private String password;
    private String providerId;
    private LocalDateTime registrationDate;
    private LocalDateTime lastModifiedDate;
    private UserRole userRole;
    private String otp;
}
