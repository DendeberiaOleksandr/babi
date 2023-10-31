package org.babi.backend.mail.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    private String host;
    private int port;
    private String username;
    private String password;
    private String transportProtocol;
    private boolean smtpAuth;
    private boolean smtpStarttlsEnable;
    private boolean debug;

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", transportProtocol);
        properties.put("mail.smtp.auth", smtpAuth);
        properties.put("mail.smtp.starttls.enable", smtpStarttlsEnable);
        properties.put("mail.debug", debug);

        return  mailSender;
    }

    @Value("${babi.email.host:smtp.gmail.com}")
    public void setHost(String host) {
        this.host = host;
    }

    @Value("${babi.email.port:587}")
    public void setPort(int port) {
        this.port = port;
    }

    @Value("${babi.email.username}")
    public void setUsername(String username) {
        this.username = username;
    }

    @Value("${babi.email.password}")
    public void setPassword(String password) {
        this.password = password;
    }

    @Value("${babi.email.transportProtocol:smtp}")
    public void setTransportProtocol(String transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    @Value("${babi.email.smtpAuth:true}")
    public void setSmtpAuth(boolean smtpAuth) {
        this.smtpAuth = smtpAuth;
    }

    @Value("${babi.email.smtpStarttlsEnable:true}")
    public void setSmtpStarttlsEnable(boolean smtpStarttlsEnable) {
        this.smtpStarttlsEnable = smtpStarttlsEnable;
    }

    @Value("${babi.email.debug:true}")
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
