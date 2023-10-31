package org.babi.backend.mail.service;

import lombok.extern.slf4j.Slf4j;
import org.babi.backend.url.builder.service.UrlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final UrlBuilder urlBuilder;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender,
                           UrlBuilder urlBuilder) {
        this.mailSender = mailSender;
        this.urlBuilder = urlBuilder;
    }

    @Override
    public boolean sendRegistrationVerificationCode(String email, String token) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("noreply@babi.org");
            mailMessage.setTo(email);
            mailMessage.setSubject("Babi Confirm Registration");
            mailMessage.setText(urlBuilder.buildRegistrationVerificationUrl(token));
            mailSender.send(mailMessage);
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }
}
