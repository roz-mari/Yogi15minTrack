package com.yogi15mintrack.yogi15mintrack.security;

import com.yogi15mintrack.yogi15mintrack.security.jwt.JwtService;
import com.yogi15mintrack.yogi15mintrack.users.Role;
import com.yogi15mintrack.yogi15mintrack.users.User;
import com.yogi15mintrack.yogi15mintrack.users.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class JwtServiceTest {
    @Test
    void generate_validate_extract() {
        var authenticationManager = Mockito.mock(AuthenticationManager.class);
        var UserRepository = Mockito.mock(UserRepository.class);
        JwtService jwtService = new JwtService(authenticationManager, UserRepository);

        User user = User.builder()
                .id(1L)
                .username("mari")
                .email("m@e")
                .password("x")
                .role(Role.USER)
                .build();

        CustomUserDetail principal = new CustomUserDetail(user);

        String token = jwtService.generateToken(principal);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isValidToken(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("mari");
    }
    @Test
    void invalid_token_should_fail_validation() {
        AuthenticationManager authenticationManager = Mockito.mock(AuthenticationManager.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);

        JwtService jwtService = new JwtService(authenticationManager, userRepository);
        String fakeToken = "abc.def.ghi";
        assertThat(jwtService.isValidToken(fakeToken)).isFalse();
    }
}
