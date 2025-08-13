package com.yogi15mintrack.yogi15mintrack.users.dto;

import com.yogi15mintrack.yogi15mintrack.users.Role;
import com.yogi15mintrack.yogi15mintrack.users.User;

public final class UserMapper {

    private UserMapper() {}

    public static User toEntity(UserRegisterRequest dto, Role role) {
        return User.builder()
                .username(dto.username())
                .email(dto.email())
                .password(dto.password())
                .role(role)
                .build();
    }

public static UserResponse toDto(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }
    public static UserResponseShort toDtoShort (User user) {
        return new UserResponseShort(user.getUsername());
    }
}
