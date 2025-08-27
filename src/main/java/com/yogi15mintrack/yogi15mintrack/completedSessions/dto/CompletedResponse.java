package com.yogi15mintrack.yogi15mintrack.completedSessions.dto;

import java.time.LocalDate;

public record CompletedResponse (
        Long id,
        Long sessionId,
        String sessionTitle,
        LocalDate dateCompleted,
        String state,
        String note
) {
}
