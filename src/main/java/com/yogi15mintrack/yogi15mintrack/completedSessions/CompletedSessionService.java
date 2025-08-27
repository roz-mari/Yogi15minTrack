package com.yogi15mintrack.yogi15mintrack.completedSessions;


import com.yogi15mintrack.yogi15mintrack.completedSessions.dto.CompletedCreateRequest;
import com.yogi15mintrack.yogi15mintrack.completedSessions.dto.CompletedResponse;
import com.yogi15mintrack.yogi15mintrack.sessions.SessionRepository;
import com.yogi15mintrack.yogi15mintrack.users.User;
import com.yogi15mintrack.yogi15mintrack.users.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.yogi15mintrack.yogi15mintrack.sessions.Session;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompletedSessionService {
    private final CompletedSessionRepository completedRepo;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepo;

    @Transactional
    public CompletedResponse completeToday(Long userId, CompletedCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Session session = sessionRepo.findById(request.sessionId())
                .orElseThrow(() -> new RuntimeException("Session not found: " + request.sessionId()));

        LocalDate today = LocalDate.now();

        if (completedRepo.existsByUserIdAndDateCompleted(userId, today)) {
            throw new IllegalStateException("Session already marked as completed today");
        }

        CompletedSession saved = completedRepo.save(
                CompletedSession.builder()
                        .user(user)
                        .session(session)
                        .dateCompleted(today)
                        .state(request.state())
                        .note(request.note())
                        .build()
        );
        return map(saved);
    }

    @Transactional(readOnly = true)
    public List<CompletedResponse> myCompleted(Long userId) {
        return completedRepo.findByUserIdOrderByDateCompletedDesc(userId)
                .stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public boolean isCompletedToday(Long userId) {
        return completedRepo.existsByUserIdAndDateCompleted(userId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public int currentStreak(Long userId) {
        LocalDate today = LocalDate.now();
        if (!completedRepo.existsByUserIdAndDateCompleted(userId, today)) {
            return 0;
        }

        int streak = 0;
        LocalDate cursor = today;

        while (completedRepo.existsByUserIdAndDateCompleted(userId, cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private CompletedResponse map(CompletedSession completedSession) {
        return new CompletedResponse(
                completedSession.getId(),
                completedSession.getSession().getId(),
                completedSession.getSession().getTitle(),
                completedSession.getDateCompleted(),
                completedSession.getState(),
                completedSession.getNote()
        );
    }
}
