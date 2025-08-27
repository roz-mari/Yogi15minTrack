package com.yogi15mintrack.yogi15mintrack.sessions;


import com.yogi15mintrack.yogi15mintrack.sessions.dto.SessionCreateRequest;
import com.yogi15mintrack.yogi15mintrack.sessions.dto.SessionResponse;
import com.yogi15mintrack.yogi15mintrack.sessions.dto.SessionUpdateRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    @Transactional
    public List<SessionResponse> getAllSessions() {
        return sessionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SessionResponse getTodaySession() {
        int today = LocalDate.now().getDayOfWeek().getValue(); // 1..7
        Session session = sessionRepository.findByDayOrder(today)
                .orElseThrow(() -> new RuntimeException("Session not found for today (day " + today + ")"));
        return toResponse(session);
    }

    @Transactional
    public SessionResponse createSession(SessionCreateRequest request) {
        Session session = Session.builder()
                .title(request.title())
                .description(request.description())
                .videoUrl(request.videoUrl())
                .dayOrder(request.dayOrder())
                .build();
        return toResponse(sessionRepository.save(session));
    }

    @Transactional
    public SessionResponse updateSession(Long id, SessionUpdateRequest request) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found: " + id));
        session.setTitle(request.title());
        session.setDescription(request.description());
        session.setVideoUrl(request.videoUrl());
        session.setDayOrder(request.dayOrder());
        return toResponse(sessionRepository.save(session));
    }

    @Transactional
    public void deleteSession(Long id) {
        sessionRepository.deleteById(id);
    }

    private SessionResponse toResponse(Session session) {
        return new SessionResponse(
                session.getId(),
                session.getTitle(),
                session.getDescription(),
                session.getVideoUrl(),
                session.getDayOrder()
        );
    }
}
