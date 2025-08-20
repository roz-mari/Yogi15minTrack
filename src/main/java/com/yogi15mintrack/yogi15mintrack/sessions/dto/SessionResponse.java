package com.yogi15mintrack.yogi15mintrack.sessions.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class SessionResponse {
    private Long id;
    private String title;
    private String description;
    private String videoUrl;
    private int dayOfWeek;
}
