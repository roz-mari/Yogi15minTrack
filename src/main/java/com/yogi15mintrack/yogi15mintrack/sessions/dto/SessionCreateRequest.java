package com.yogi15mintrack.yogi15mintrack.sessions.dto;

public record SessionCreateRequest (
        String title,
        String description,
        String videoUrl,
        int dayOrder
) {
}
