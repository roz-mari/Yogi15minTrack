package com.yogi15mintrack.yogi15mintrack.sessions;


import com.yogi15mintrack.yogi15mintrack.sessions.dto.SessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;

    public List<SessionResponse> getAllSessions() {
        return sessionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SessionResponse getTodaySession() {
        int today = LocalDate.now().getDayOfWeek().getValue(); // 1=Пн..7=Вс
        return sessionRepository.findByDayOfWeek(today)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Session not found for today"));
    }

    public SessionResponse createSession(SessionResponse req) {
        Session session = new Session(null, req.getTitle(), req.getDescription(), req.getVideoUrl(), req.getDayOfWeek());
        return toResponse(sessionRepository.save(session));
    }

    public SessionResponse updateSession(Long id, SessionResponse req) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setTitle(req.getTitle());
        session.setDescription(req.getDescription());
        session.setVideoUrl(req.getVideoUrl());
        session.setDayOfWeek(req.getDayOfWeek());
        return toResponse(sessionRepository.save(session));
    }

    public void deleteSession(Long id) {
        sessionRepository.deleteById(id);
    }

    private SessionResponse toResponse(Session s) {
        return new SessionResponse(s.getId(), s.getTitle(), s.getDescription(), s.getVideoUrl(), s.getDayOfWeek());
    }
}
