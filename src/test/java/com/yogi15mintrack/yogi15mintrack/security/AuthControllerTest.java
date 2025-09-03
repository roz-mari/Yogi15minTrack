package com.yogi15mintrack.yogi15mintrack.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yogi15mintrack.yogi15mintrack.security.jwt.JwtResponse;
import com.yogi15mintrack.yogi15mintrack.security.jwt.JwtService;
import com.yogi15mintrack.yogi15mintrack.users.Role;
import com.yogi15mintrack.yogi15mintrack.users.User;
import com.yogi15mintrack.yogi15mintrack.users.UserService;
import com.yogi15mintrack.yogi15mintrack.users.dto.UserLoginRequest;
import com.yogi15mintrack.yogi15mintrack.users.dto.UserRegisterRequest;
import com.yogi15mintrack.yogi15mintrack.users.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtService jwtService;

    private UserDetails adminPrincipal;
    private UserDetails userPrincipal;

    private static String convertObjectToJson(Object object, ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to convert object to JSON", exception);
        }
    }

    @BeforeEach
    void setUp() {
        User admin = User.builder()
                .id(1L).username("admin").email("admin@yogi15.com")
                .password("encoded").role(Role.ADMIN)
                .build();

        User user = User.builder()
                .id(2L).username("maria").email("m@e")
                .password("encoded").role(Role.USER)
                .build();

        adminPrincipal = new CustomUserDetail(admin);
        userPrincipal = new CustomUserDetail(user);
    }

    @Nested
    @DisplayName("POST /auth/register")
    class RegisterUserTests {

        @Test
        @DisplayName("200 OK + body")
        void register_ok() throws Exception {
            UserRegisterRequest request = new UserRegisterRequest("john", "john@gmail.com", "Passw0rd!");
            UserResponse response = new UserResponse(10L, "john", "john@gmail.com", "ROLE_USER");

            given(userService.addUser(request)).willReturn(response);

            mockMvc.perform(post("/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(convertObjectToJson(request, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(convertObjectToJson(response, objectMapper)));

            verify(userService, times(1)).addUser(request);
        }

        @Test
        @DisplayName("400 when body invalid")
        void register_bad_request() throws Exception {
            UserRegisterRequest invalid = new UserRegisterRequest("john", "john@gmail.com", "");

            mockMvc.perform(post("/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(convertObjectToJson(invalid, objectMapper)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /auth/login")
    class LoginTests {

        @Test
        @DisplayName("200 OK + token")
        void login_ok() throws Exception {
            UserLoginRequest request = new UserLoginRequest("john", "password");
            JwtResponse jwtResponse = new JwtResponse("test-token");

            given(jwtService.loginAuthentication(request)).willReturn(jwtResponse);

            mockMvc.perform(post("/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(convertObjectToJson(request, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(convertObjectToJson(jwtResponse, objectMapper)));

            verify(jwtService, times(1)).loginAuthentication(request);
        }

        @Test
        @DisplayName("401 when bad credentials")
        void login_unauthorized() throws Exception {
            UserLoginRequest request = new UserLoginRequest("john", "wrong");

            given(jwtService.loginAuthentication(request)).willThrow(new BadCredentialsException("bad"));

            mockMvc.perform(post("/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(convertObjectToJson(request, objectMapper)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /auth/register-admin")
    class RegisterAdminTests {

        @Test
        @DisplayName("ADMIN can register admin -> 200 OK")
        void registerAdmin_admin_ok() throws Exception {
            UserRegisterRequest request =
                    new UserRegisterRequest("anna", "anna@yogi.com", "Passw0rd!");
            UserResponse response =
                    new UserResponse(11L, "anna", "anna@yogi.com", "ROLE_ADMIN");

            given(userService.addAdmin(request)).willReturn(response);

            mockMvc.perform(post("/auth/register-admin")
                            .with(user(adminPrincipal))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(convertObjectToJson(request, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(convertObjectToJson(response, objectMapper)));

            verify(userService, times(1)).addAdmin(request);
        }

        @Test
        @DisplayName("unauthenticated -> 401")
        void registerAdmin_noAuth() throws Exception {
            var request = new UserRegisterRequest("anna", "anna@yogi.com", "Passw0rd!");
            mockMvc.perform(post("/auth/register-admin")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(convertObjectToJson(request, objectMapper)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("USER role -> 403")
        void registerAdmin_user_forbidden() throws Exception {
            var request = new UserRegisterRequest("anna", "anna@yogi.com", "Passw0rd!");
            mockMvc.perform(post("/auth/register-admin")
                            .with(user(userPrincipal))   // ROLE_USER
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(convertObjectToJson(request, objectMapper)))
                    .andExpect(status().isForbidden());
        }

        @Nested
        @DisplayName("GET /auth/me")
        class MeTests {

            @Test
            @DisplayName("200 OK with profile")
            void me_ok() throws Exception {
                UserResponse my = new UserResponse(2L, "maria", "m@e", "ROLE_USER");
                given(userService.getOwnUser(2L)).willReturn(my);

                mockMvc.perform(get("/auth/me")
                                .with(user(userPrincipal)))
                        .andExpect(status().isOk())
                        .andExpect(content().json(convertObjectToJson(my, objectMapper)));
            }

            @Test
            @DisplayName("401 when not authenticated")
            void me_unauth() throws Exception {
                mockMvc.perform(get("/auth/me"))
                        .andExpect(status().isUnauthorized());
            }
        }
    }
}