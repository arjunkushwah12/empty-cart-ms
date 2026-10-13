package com.emptycart.user_service.bootstrap;


import com.emptycart.user_service.domain.Role;
import com.emptycart.user_service.domain.User;
import com.emptycart.user_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;

    public AdminInitializer(UserRepository users, PasswordEncoder encoder,
                            @Value("${admin.email:}") String email,
                            @Value("${admin.password:}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.existsByRole(Role.ADMIN)) return;
        if (email.isBlank() || password.isBlank()) {
            log.warn("No admin exists and ADMIN_EMAIL / ADMIN_PASSWORD are not set");
            return;
        }
        users.save(new User("Administrator", email.trim().toLowerCase(), encoder.encode(password), Role.ADMIN));
        log.info("Created initial admin {}", email);
    }
}