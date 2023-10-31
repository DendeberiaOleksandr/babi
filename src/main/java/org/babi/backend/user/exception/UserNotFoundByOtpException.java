package org.babi.backend.user.exception;

public class UserNotFoundByOtpException extends RuntimeException {

    private final String otp;

    public UserNotFoundByOtpException(String otp) {
        super(String.format("User not found by otp: %s", otp));
        this.otp = otp;
    }

    public String getOtp() {
        return otp;
    }
}
