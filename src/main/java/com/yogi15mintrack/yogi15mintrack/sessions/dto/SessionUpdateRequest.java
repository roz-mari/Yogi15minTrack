package com.yogi15mintrack.yogi15mintrack.sessions.dto;

public record SessionUpdateRequest(
        String title,
        String description,
        String videoUrl,
        Integer dayOrder
) {}