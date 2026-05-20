package com.hotel.hotelbooking;

import com.hotel.hotelbooking.entity.User;
import com.hotel.hotelbooking.repository.UserRepository;
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
        if (!userRepository.existsByEmail("admin@hotel.com")) {
            User admin = User.builder()
                    .fullName("Hotel Admin")
                    .email("admin@hotel.com")
                    .password(passwordEncoder.encode("admin123"))
                    .phone("9999999999")
                    .role(User.Role.ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println("✅ Admin created: admin@hotel.com / admin123");
        }
    }
}