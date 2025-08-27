package com.yogi15mintrack.yogi15mintrack.sessions;

import com.yogi15mintrack.yogi15mintrack.sessions.dto.SessionCreateRequest;
import com.yogi15mintrack.yogi15mintrack.sessions.dto.SessionResponse;
import com.yogi15mintrack.yogi15mintrack.sessions.dto.SessionUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SessionResponse>> getAllSessions() {
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    @GetMapping("/today")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SessionResponse> getTodaySession() {
        return ResponseEntity.ok(sessionService.getTodaySession());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessionResponse> createSession(@RequestBody SessionCreateRequest request) {
        return ResponseEntity.ok(sessionService.createSession(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessionResponse> updateSession(@PathVariable Long id, @RequestBody SessionUpdateRequest request) {
        return ResponseEntity.ok(sessionService.updateSession(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
