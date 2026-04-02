package com.zorvyn.financebackend.config;

import com.zorvyn.financebackend.model.User;
import com.zorvyn.financebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TestUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.seed-test-users:true}")
    private boolean seedTestUsers;

    public TestUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!seedTestUsers) {
            return;
        }

        createUserIfMissing("Admin Test", "admin@zorvyn.local", "admin123", User.Role.ADMIN);
        createUserIfMissing("Analyst Test", "analyst@zorvyn.local", "analyst123", User.Role.ANALYST);
        createUserIfMissing("Viewer Test", "viewer@zorvyn.local", "viewer123", User.Role.VIEWER);
    }

    private void createUserIfMissing(String name, String email, String rawPassword, User.Role role) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .status(User.Status.ACTIVE)
                .build();

        userRepository.save(user);
    }
}
