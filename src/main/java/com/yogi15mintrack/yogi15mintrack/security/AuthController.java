package com.yogi15mintrack.yogi15mintrack.security;

import com.yogi15mintrack.yogi15mintrack.security.jwt.JwtService;
import com.yogi15mintrack.yogi15mintrack.users.UserService;
import com.yogi15mintrack.yogi15mintrack.users.dto.UserLoginRequest;
import com.yogi15mintrack.yogi15mintrack.users.dto.UserRegisterRequest;
import com.yogi15mintrack.yogi15mintrack.users.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.yogi15mintrack.yogi15mintrack.security.jwt.JwtResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserRegisterRequest request) {
        UserResponse response = userService.addUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> registerAdmin(@RequestBody @Valid UserRegisterRequest request) {
        UserResponse response = userService.addAdmin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody @Valid UserLoginRequest request) {
        JwtResponse jwt = jwtService.loginAuthentication(request);
        return ResponseEntity.ok(jwt);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetail principal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse response = userService.getOwnUser(principal.getId());
        return ResponseEntity.ok(response);
    }
}

