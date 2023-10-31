package org.babi.backend.user.config;

import lombok.extern.slf4j.Slf4j;
import org.babi.backend.user.domain.User;
import org.babi.backend.user.domain.UserRole;
import org.babi.backend.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Slf4j
@Configuration
public class UserBootstrapConfig {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserBootstrapConfig(UserService userService,
                               PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerAdminUser() {
      log.info("Registering admin user");
      String adminUsername = "admin";
      userService.findByUsername(adminUsername)
                      .onErrorResume(throwable -> {
                          log.info("Admin user is not registered so starting registering a new one.");
                          return userService.save(new User(
                                  null, adminUsername, "email@emal.com", true, true, passwordEncoder.encode("admin"),
                                  null, LocalDateTime.now(), LocalDateTime.now(), UserRole.ADMIN, null
                          ));
                      })
                      .subscribe(user -> log.info("Successfully registered admin user: {}/{}", adminUsername, "admin"));
    }

}
