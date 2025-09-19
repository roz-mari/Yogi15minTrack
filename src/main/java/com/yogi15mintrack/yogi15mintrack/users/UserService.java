package com.yogi15mintrack.yogi15mintrack.users;

import com.yogi15mintrack.yogi15mintrack.exceptions.EntityAlreadyExistsException;
import com.yogi15mintrack.yogi15mintrack.exceptions.EntityNotFoundException;
import com.yogi15mintrack.yogi15mintrack.security.CustomUserDetail;
import com.yogi15mintrack.yogi15mintrack.users.dto.UserMapper;
import com.yogi15mintrack.yogi15mintrack.users.dto.UserRegisterRequest;
import com.yogi15mintrack.yogi15mintrack.users.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserByIdAdmin(Long id) {
        return getUserById(id);
    }

    @PreAuthorize("isAuthenticated()")
    public UserResponse getOwnUser(Long id) {
        return getUserById(id);
    }

    private UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(User.class.getSimpleName(), "id", id.toString()));
        return UserMapper.toDto(user);
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException(User.class.getSimpleName(), "username", username));
    }

    public UserResponse addUser(UserRegisterRequest request) {
        return addUserByRole(request, Role.USER);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse addAdmin(UserRegisterRequest request) {
        return addUserByRole(request, Role.ADMIN);
    }

    private UserResponse addUserByRole(UserRegisterRequest request, Role role) {
        if (userRepository.existsByUsername(request.username())) {
            throw new EntityAlreadyExistsException(User.class.getSimpleName(), "username", request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new EntityAlreadyExistsException(User.class.getSimpleName(), "email", request.email());
        }

        User user = UserMapper.toEntity(request, role);
        user.setPassword(passwordEncoder.encode(request.password()));

        return UserMapper.toDto(userRepository.save(user));
    }

    @PreAuthorize("isAuthenticated()")
    public UserResponse updateOwnUser(Long id, UserRegisterRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(User.class.getSimpleName(), "id", id.toString()));

        if (!existingUser.getUsername().equals(request.username())
                && userRepository.findByUsername(request.username()).isPresent()) {
            throw new EntityAlreadyExistsException(User.class.getSimpleName(), "username", request.username());
        }
        if (!existingUser.getEmail().equals(request.email())
                && userRepository.existsByEmail(request.email())) {
            throw new EntityAlreadyExistsException(User.class.getSimpleName(), "email", request.email());
        }

        existingUser.setUsername(request.username());
        existingUser.setEmail(request.email());
        existingUser.setPassword(passwordEncoder.encode(request.password()));
        return UserMapper.toDto(userRepository.save(existingUser));
    }

    @PreAuthorize("isAuthenticated()")
    public String deleteOwnUser(Long id) {
        return deleteUserById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUserByIdAdmin(Long id) {
        return deleteUserById(id);
    }

    private String deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException(User.class.getSimpleName(), "id", id.toString());
        }
        userRepository.deleteById(id);
        return "User with id " + id + " deleted successfully";
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new CustomUserDetail(user);
    }
}


