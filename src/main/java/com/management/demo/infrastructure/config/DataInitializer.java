package com.management.demo.infrastructure.config;

import com.management.demo.domain.entity.User;
import com.management.demo.domain.enums.Role;
import com.management.demo.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(User.builder()
                    .username("admin")
                    .email("admin@demo.com")
                    .name("Administrador")
                    .password(passwordEncoder.encode("pwd@159753"))
                    .role(Role.ADMIN)
                    .active(true)
                    .build());
        }
    }
}
