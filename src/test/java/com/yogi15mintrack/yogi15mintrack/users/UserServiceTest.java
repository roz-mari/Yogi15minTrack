package com.yogi15mintrack.yogi15mintrack.users;

import com.yogi15mintrack.yogi15mintrack.users.dto.UserRegisterRequest;
import com.yogi15mintrack.yogi15mintrack.users.dto.UserResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
    class UserServiceTest {

        @Mock
        private UserRepository userRepository;
        @Mock private PasswordEncoder passwordEncoder;

        @InjectMocks
        private UserService userService;

        private User existingUser;
        private UserRegisterRequest registerRequest;
        private UserResponse expectedResponse;

        @BeforeEach
        void setUp() {
            existingUser = User.builder()
                    .id(1L)
                    .username("Kate")
                    .email("kate.dev@gmail.com")
                    .password("encoded-password")
                    .role(Role.USER)
                    .build();

            registerRequest = new UserRegisterRequest("Kate", "kate.dev@gmail.com", "mypass1234*");
            expectedResponse = new UserResponse(1L, "Kate", "kate.dev@gmail.com", "USER");
        }

        @AfterEach
        void tearDown() {
            verifyNoMoreInteractions(userRepository, passwordEncoder);
        }

        @Test
        void getAllUsers_returnsList() {
            when(userRepository.findAll()).thenReturn(List.of(existingUser));

            var result = userService.getAllUsers();

            assertThat(result).containsExactly(expectedResponse);
            verify(userRepository, times(1)).findAll();
        }

        @Test
        void getAllUsers_empty_whenNoUsers() {
            when(userRepository.findAll()).thenReturn(List.of());

            var result = userService.getAllUsers();

            assertThat(result).isEmpty();
            verify(userRepository).findAll();
        }

        @Test
        void getUserByIdAdmin_ok() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

            var result = userService.getUserByIdAdmin(1L);

            assertThat(result).isEqualTo(expectedResponse);
            verify(userRepository).findById(1L);
        }

        @Test
        void getUserByIdAdmin_notFound_throws() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> userService.getUserByIdAdmin(99L));
            verify(userRepository).findById(99L);
        }

        @Test
        void getOwnUser_ok() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

            var result = userService.getOwnUser(1L);

            assertThat(result).isEqualTo(expectedResponse);
            verify(userRepository).findById(1L);
        }

        @Test
        void getByUsername_ok() {
            when(userRepository.findByUsername("Kate")).thenReturn(Optional.of(existingUser));

            var result = userService.getByUsername("Kate");

            assertThat(result).isEqualTo(existingUser);
            verify(userRepository).findByUsername("Kate");
        }

        @Test
        void getByUsername_notFound_throws() {
            when(userRepository.findByUsername("Mike")).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> userService.getByUsername("Mike"));
            verify(userRepository).findByUsername("Mike");
        }

        @Test
        void addUser_ok_encodesPassword_andSaves() {
            when(userRepository.existsByUsername(registerRequest.username())).thenReturn(false);
            when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
            when(passwordEncoder.encode(registerRequest.password())).thenReturn("encoded");
            // имитируем сохранение — пароль уже заэнкожен в маппере/сервисе
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(1L);
                return u;
            });

            var result = userService.addUser(registerRequest);

            assertThat(result).isEqualTo(expectedResponse);
            verify(userRepository).existsByUsername(registerRequest.username());
            verify(userRepository).existsByEmail(registerRequest.email());
            verify(passwordEncoder).encode(registerRequest.password());
            verify(userRepository).save(any(User.class));
        }

        @Test
        void addUser_duplicateUsername_throws() {
            when(userRepository.existsByUsername(registerRequest.username())).thenReturn(true);

            assertThrows(RuntimeException.class, () -> userService.addUser(registerRequest));
            verify(userRepository).existsByUsername(registerRequest.username());
        }

        @Test
        void addUser_duplicateEmail_throws() {
            when(userRepository.existsByUsername(registerRequest.username())).thenReturn(false);
            when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

            assertThrows(RuntimeException.class, () -> userService.addUser(registerRequest));
            verify(userRepository).existsByUsername(registerRequest.username());
            verify(userRepository).existsByEmail(registerRequest.email());
        }

        @Test
        void updateOwnUser_ok_usernameSame_emailSame_passwordEncoded() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.encode(registerRequest.password())).thenReturn("encoded-new");
            when(userRepository.save(any(User.class))).thenReturn(existingUser);

            var result = userService.updateOwnUser(1L, registerRequest);

            assertThat(result.username()).isEqualTo("Kate");
            verify(userRepository).findById(1L);
            verify(passwordEncoder).encode(registerRequest.password());
            verify(userRepository).save(any(User.class));
        }

        @Test
        void updateOwnUser_notFound_throws() {
            when(userRepository.findById(5L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> userService.updateOwnUser(5L, registerRequest));
            verify(userRepository).findById(5L);
        }

        @Test
        void updateOwnUser_usernameTaken_throws() {
            var request = new UserRegisterRequest("NewKate", "kate.dev@gmail.com", "mypass1234*");
            var otherUser = User.builder()
                    .id(2L).username("NewKate").email("other@e").password("p").role(Role.USER).build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(userRepository.findByUsername("NewKate")).thenReturn(Optional.of(otherUser));

            assertThrows(RuntimeException.class, () -> userService.updateOwnUser(1L, request));

            verify(userRepository).findById(1L);
            verify(userRepository).findByUsername("NewKate");
            // В этом сценарии save вызываться НЕ должен:
            verify(userRepository, never()).save(any());
        }

        @Test
        void updateOwnUser_emailTaken_throws() {
            var request = new UserRegisterRequest(
                    existingUser.getUsername(),      // имя можно не менять
                    "new-email@yogi.com",            // ВАЖНО: другой email
                    "newPass123*"
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByEmail("new-email@yogi.com")).thenReturn(true);

            assertThrows(RuntimeException.class, () -> userService.updateOwnUser(1L, request));

            verify(userRepository).findById(1L);
            verify(userRepository).existsByEmail("new-email@yogi.com");
            verify(userRepository, never()).save(any()); // при ошибке сохранения быть не должно
        }

        @Test
        void deleteUserByIdAdmin_ok() {
            when(userRepository.existsById(1L)).thenReturn(true);
            doNothing().when(userRepository).deleteById(1L);

            var msg = userService.deleteUserByIdAdmin(1L);

            assertThat(msg).isEqualTo("User with id 1 deleted successfully");
            verify(userRepository).existsById(1L);
            verify(userRepository).deleteById(1L);
        }

        @Test
        void deleteUserByIdAdmin_notFound_throws() {
            when(userRepository.existsById(1L)).thenReturn(false);

            assertThrows(RuntimeException.class, () -> userService.deleteUserByIdAdmin(1L));
            verify(userRepository).existsById(1L);
        }

        @Test
        void deleteOwnUser_ok() {
            when(userRepository.existsById(1L)).thenReturn(true);
            doNothing().when(userRepository).deleteById(1L);

            var msg = userService.deleteOwnUser(1L);

            assertThat(msg).isEqualTo("User with id 1 deleted successfully");
            verify(userRepository).existsById(1L);
            verify(userRepository).deleteById(1L);
        }

        @Test
        void deleteOwnUser_notFound_throws() {
            when(userRepository.existsById(1L)).thenReturn(false);

            assertThrows(RuntimeException.class, () -> userService.deleteOwnUser(1L));
            verify(userRepository).existsById(1L);
        }

        @Test
        void loadUserByUsername_ok_returnsCustomUserDetail() {
            when(userRepository.findByUsername("Kate")).thenReturn(Optional.of(existingUser));

            UserDetails details = userService.loadUserByUsername("Kate");

            assertThat(details.getUsername()).isEqualTo("Kate");
            assertThat(details.getPassword()).isEqualTo("encoded-password");
            assertThat(details.getAuthorities()).isNotEmpty();
            verify(userRepository).findByUsername("Kate");
        }

    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFound() {
        when(userRepository.findByUsername("Mike")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("Mike"));
        verify(userRepository).findByUsername("Mike");
    }
    }

