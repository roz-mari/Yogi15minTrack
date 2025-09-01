package com.yogi15mintrack.yogi15mintrack.completedSessions.dto;

import com.yogi15mintrack.yogi15mintrack.completedSessions.Mood;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompletedCreateRequest(
        @NotNull Long sessionId,
        @NotNull Mood state,
        String note
) {}

