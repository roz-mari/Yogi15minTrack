package com.yogi15mintrack.yogi15mintrack.users.dto;

public record UserResponse (
        Long id,
        String username,
        String email,
        String role
) {
}
