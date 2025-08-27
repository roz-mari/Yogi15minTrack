package com.yogi15mintrack.yogi15mintrack.users;

import com.yogi15mintrack.yogi15mintrack.users.dto.UserRegisterRequest;
import com.yogi15mintrack.yogi15mintrack.users.dto.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserByIdAdmin(Long id);
    UserResponse getOwnUser(Long id);
    User getByUsername(String username);
    UserResponse addUser(UserRegisterRequest request);
    UserResponse addAdmin(UserRegisterRequest request);
    UserResponse updateOwnUser(Long id, UserRegisterRequest request);
    String deleteOwnUser(Long id);
    String deleteUserByIdAdmin(Long id);
}
