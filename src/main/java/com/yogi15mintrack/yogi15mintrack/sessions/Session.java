package com.yogi15mintrack.yogi15mintrack.sessions;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String videoUrl;

    @Column(nullable = false)
    private int dayOrder;
}

