package com.yogi15mintrack.yogi15mintrack.users;

import com.yogi15mintrack.yogi15mintrack.security.CustomUserDetail;
import com.yogi15mintrack.yogi15mintrack.sessions.SessionService;
import com.yogi15mintrack.yogi15mintrack.users.dto.UserRegisterRequest;
import com.yogi15mintrack.yogi15mintrack.users.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.yogi15mintrack.yogi15mintrack.sessions.dto.SessionResponse;
import com.yogi15mintrack.yogi15mintrack.sessions.dto.SessionResponse;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SessionService sessionService;

    @GetMapping("/me/session/today")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SessionResponse> getTodaySession(
            @AuthenticationPrincipal CustomUserDetail userDetail) {
        return ResponseEntity.ok(sessionService.getTodaySession());
    }


    @GetMapping("/me/sessions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SessionResponse>> getAllAvailableSessions() {
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getMyUser(
            @AuthenticationPrincipal CustomUserDetail userDetail) {
        return ResponseEntity.ok(userService.getOwnUser(userDetail.getId()));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateMyUser(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestBody @Valid UserRegisterRequest request) {
        return ResponseEntity.ok(userService.updateOwnUser(userDetail.getId(), request));
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteMyUser(
            @AuthenticationPrincipal CustomUserDetail userDetail) {
        return ResponseEntity.ok(userService.deleteOwnUser(userDetail.getId()));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody @Valid UserRegisterRequest request) {
        return ResponseEntity.ok(userService.addUser(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsersAdmin() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserByIdAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserByIdAdmin(id));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> addAdmin(@RequestBody @Valid UserRegisterRequest request) {
        return ResponseEntity.ok(userService.addAdmin(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUserByIdAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUserByIdAdmin(id));
    }
}
