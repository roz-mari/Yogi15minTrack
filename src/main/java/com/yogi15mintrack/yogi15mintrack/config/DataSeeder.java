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
                        Session.builder().title("Day 1 – Morning Yoga for Flexibility")
                                .description("Sweet Release SLOW FLOW Deep Stretch")
                                .videoUrl("https://www.youtube.com/watch?v=zLer5A9LgMs")
                                .dayOrder(1)
                                .build(),
                        Session.builder().title("Day 2 – Gentle Yoga for Hip Flexibility")
                                .description("Hips deep stretch")
                                .videoUrl("https://www.youtube.com/watch?v=-trGf8O3ue4")
                                .dayOrder(2)
                                .build(),
                        Session.builder().title("Day 3 – Gentle Morning Yoga")
                                .description("Lower Back Stretches")
                                .videoUrl("https://www.youtube.com/watch?v=VlUfKp1LKpc")
                                .dayOrder(3)
                                .build(),
                        Session.builder().title("Day 4 – Morning Yoga Flow")
                                .description("Daily Stretch & Strength Routine")
                                .videoUrl("https://www.youtube.com/watch?v=mlSMNkTou0k")
                                .dayOrder(4)
                                .build(),
                        Session.builder().title("Day 5 – Gentle Morning Yoga")
                                .description("Full Body Stretch")
                                .videoUrl("https://www.youtube.com/watch?v=Y8vJqiePu1I")
                                .dayOrder(5)
                                .build(),
                        Session.builder().title("Day 6 – Morning Power Yoga Flow")
                                .description("Yoga with Blocks")
                                .videoUrl("https://www.youtube.com/watch?v=wFNMSal96Xs")
                                .dayOrder(6)
                                .build(),
                        Session.builder().title("Day 7 – Morning Yoga Stretch")
                                .description("Hands & Wrists Free Yoga")
                                .videoUrl("https://www.youtube.com/watch?v=qFxl9NKkgDU")
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

