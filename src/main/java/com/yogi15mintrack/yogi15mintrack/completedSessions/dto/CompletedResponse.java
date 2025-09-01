package com.yogi15mintrack.yogi15mintrack.completedSessions.dto;

import com.yogi15mintrack.yogi15mintrack.completedSessions.Mood;

import java.time.LocalDate;

public record CompletedResponse (
        Long id,
        Long sessionId,
        String sessionTitle,
        LocalDate dateCompleted,
        Mood state,
        String note
) {
}
