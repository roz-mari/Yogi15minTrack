package com.yogi15mintrack.yogi15mintrack.config;

import com.yogi15mintrack.yogi15mintrack.sessions.Session;
import com.yogi15mintrack.yogi15mintrack.sessions.SessionRepository;
import com.yogi15mintrack.yogi15mintrack.users.Role;
import com.yogi15mintrack.yogi15mintrack.users.User;
import com.yogi15mintrack.yogi15mintrack.users.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataSeeder {
    @Bean
    public CommandLineRunner seedSampleData(
            UserRepository userRepository,
            SessionRepository sessionRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = User.builder()
                        .username("admin")
                        .email("admin@yogi15.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .build();

                User user = User.builder()
                        .username("user")
                        .email("user@yogi15.com")
                        .password(passwordEncoder.encode("user123"))
                        .role(Role.USER)
                        .build();

                userRepository.saveAll(List.of(admin, user));
                System.out.println("Sample users seeded.");
            } else {
                System.out.println("Users already seeded.");
            }

            if (sessionRepository.count() == 0) {
                List<Session> sessions = List.of(
                        Session.builder().title("Day 1 – Morning Flow")
                                .description("Gentle 15-min morning yoga")
                                .videoUrl("https://example.com/videos/day1.mp4")
                                .dayOrder(1)
                                .build(),
                        Session.builder().title("Day 2 – Core & Balance")
                                .description("Core activation and balance")
                                .videoUrl("https://example.com/videos/day2.mp4")
                                .dayOrder(2)
                                .build(),
                        Session.builder().title("Day 3 – Hip Opener")
                                .description("Hip mobility focus")
                                .videoUrl("https://example.com/videos/day3.mp4")
                                .dayOrder(3)
                                .build(),
                        Session.builder().title("Day 4 – Strength Flow")
                                .description("Strength-oriented short flow")
                                .videoUrl("https://example.com/videos/day4.mp4")
                                .dayOrder(4)
                                .build(),
                        Session.builder().title("Day 5 – Stretch & Relax")
                                .description("Full-body stretch")
                                .videoUrl("https://example.com/videos/day5.mp4")
                                .dayOrder(5)
                                .build(),
                        Session.builder().title("Day 6 – Balance & Focus")
                                .description("Balance practice to improve focus")
                                .videoUrl("https://example.com/videos/day6.mp4")
                                .dayOrder(6)
                                .build(),
                        Session.builder().title("Day 7 – Restorative")
                                .description("Gentle restorative sequence")
                                .videoUrl("https://example.com/videos/day7.mp4")
                                .dayOrder(7)
                                .build()
                );
                sessionRepository.saveAll(sessions);
                System.out.println("Sample sessions seeded.");
            } else {
                System.out.println("Sessions already seeded.");
            }
        };
    }
}

