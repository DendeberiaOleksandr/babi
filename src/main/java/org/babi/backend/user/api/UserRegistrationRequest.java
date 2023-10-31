package org.babi.backend.user.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserRegistrationRequest {

    private String email;
    private String password;
    private boolean isServiceTermsAccepted;

}
