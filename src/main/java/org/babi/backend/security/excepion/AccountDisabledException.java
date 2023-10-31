package org.babi.backend.security.excepion;

public class AccountDisabledException extends RuntimeException {

    public AccountDisabledException() {
    }

    public AccountDisabledException(String message) {
        super(message);
    }
}
