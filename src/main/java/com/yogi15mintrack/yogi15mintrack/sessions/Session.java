package com.yogi15mintrack.yogi15mintrack.sessions;

import com.yogi15mintrack.yogi15mintrack.video.Video;
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

    private String title;
    private String description;
    private String videoUrl;
    private int dayOfWeek;
}

