package org.babi.backend.mail.service;

public interface MailService {

    boolean sendRegistrationVerificationCode(String email, String code);

}
