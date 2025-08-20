package com.yogi15mintrack.yogi15mintrack.completedSessions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompletedCreateRequest(
        @NotNull Long sessionId,
        @NotBlank String state,
        String note
) {}

