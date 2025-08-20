package com.yogi15mintrack.yogi15mintrack.completedSessions;

import com.yogi15mintrack.yogi15mintrack.completedSessions.dto.CompletedCreateRequest;
import com.yogi15mintrack.yogi15mintrack.completedSessions.dto.CompletedResponse;
import com.yogi15mintrack.yogi15mintrack.security.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CompletedSessionController {

    private final CompletedSessionService service;
    @PostMapping("/completed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CompletedResponse> completeToday(
            @AuthenticationPrincipal CustomUserDetail user,
            @RequestBody
            @Valid CompletedCreateRequest request) {
        return ResponseEntity.ok(service.completeToday(user.getId(), request));
    }

    @GetMapping("/completed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CompletedResponse>> myCompleted(
            @AuthenticationPrincipal CustomUserDetail user) {
        return ResponseEntity.ok(service.myCompleted(user.getId()));
    }

    @GetMapping("/completed/today")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> completedToday(
            @AuthenticationPrincipal CustomUserDetail user) {
        return ResponseEntity.ok(service.isCompletedToday(user.getId()));
    }

    @GetMapping("/streaks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Integer> streak(
            @AuthenticationPrincipal CustomUserDetail user) {
        return ResponseEntity.ok(service.currentStreak(user.getId()));
    }
}
