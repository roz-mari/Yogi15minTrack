package com.yogi15mintrack.yogi15mintrack.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yogi15mintrack.yogi15mintrack.security.CustomUserDetail;
import com.yogi15mintrack.yogi15mintrack.sessions.SessionService;
import com.yogi15mintrack.yogi15mintrack.sessions.dto.SessionResponse;
import com.yogi15mintrack.yogi15mintrack.users.dto.UserRegisterRequest;
import com.yogi15mintrack.yogi15mintrack.users.dto.UserResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UserService userService;
    @MockitoBean private SessionService sessionService;

    private CustomUserDetail adminPrincipal;
    private CustomUserDetail userPrincipal;

    private User adminUserEntity;
    private User regularUserEntity;

    private UserResponse userResponse;
    private SessionResponse sessionResponse;

    private static String toJson(Object obj, ObjectMapper mapper) {
        try { return mapper.writeValueAsString(obj); }
        catch (Exception e) { throw new RuntimeException("Failed to serialize JSON", e); }
    }

    @BeforeEach
    void setUp() {
        adminUserEntity = User.builder()
                .id(1L).username("admin").email("admin@yogi.com")
                .password("encoded").role(Role.ADMIN).build();
        regularUserEntity = User.builder()
                .id(2L).username("kate").email("k@e")
                .password("encoded").role(Role.USER).build();

        adminPrincipal = new CustomUserDetail(adminUserEntity);
        userPrincipal  = new CustomUserDetail(regularUserEntity);

        userResponse = new UserResponse(2L, "kate", "k@e", "ROLE_USER");
        sessionResponse = new SessionResponse(
                10L, "Morning Yoga", "Gentle start", "http://video", 1
        );
    }

    @Nested
    @DisplayName("GET /users/me/session/today")
    class GetTodaySessionForMe {

        @Test
        @DisplayName("authenticated -> 200 + body")
        void today_authenticated_ok() throws Exception {
            given(sessionService.getTodaySession()).willReturn(sessionResponse);

            mockMvc.perform(get("/users/me/session/today")
                            .with(user(userPrincipal)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(toJson(sessionResponse, objectMapper)));

            verify(sessionService, times(1)).getTodaySession();
        }

        @Test
        @DisplayName("unauthenticated -> 401")
        void today_unauthenticated_401() throws Exception {
            mockMvc.perform(get("/users/me/session/today"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /users/me/sessions")
    class GetAllSessionsForMe {

        @Test
        @DisplayName("authenticated -> 200 + list body")
        void all_authenticated_ok() throws Exception {
            given(sessionService.getAllSessions()).willReturn(List.of(sessionResponse));

            mockMvc.perform(get("/users/me/sessions")
                            .with(user(userPrincipal)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(toJson(List.of(sessionResponse), objectMapper)));

            verify(sessionService, times(1)).getAllSessions();
        }

        @Test
        @DisplayName("unauthenticated -> 401")
        void all_unauthenticated_401() throws Exception {
            mockMvc.perform(get("/users/me/sessions"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /users/me")
    class GetMe {

        @Test
        @DisplayName("authenticated -> 200 + profile")
        void me_authenticated_ok() throws Exception {
            given(userService.getOwnUser(regularUserEntity.getId())).willReturn(userResponse);

            mockMvc.perform(get("/users/me")
                            .with(user(userPrincipal)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(toJson(userResponse, objectMapper)));

            verify(userService, times(1)).getOwnUser(eq(regularUserEntity.getId()));
        }

        @Test
        @DisplayName("unauthenticated -> 401")
        void me_unauthenticated_401() throws Exception {
            mockMvc.perform(get("/users/me"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /users/me")
    class UpdateMe {

        @Test
        @DisplayName("authenticated + valid body -> 200 + updated")
        void update_authenticated_ok() throws Exception {
            UserRegisterRequest valid =
                    new UserRegisterRequest("kate2", "k2@example.com", "StrongPass1!");

            given(userService.updateOwnUser(regularUserEntity.getId(), valid))
                    .willReturn(new UserResponse(2L, "kate2", "k2@example.com", "ROLE_USER"));

            mockMvc.perform(put("/users/me")
                            .with(user(userPrincipal))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(valid, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                {"id":2,"username":"kate2","email":"k2@example.com","role":"ROLE_USER"}
            """));

            verify(userService, times(1))
                    .updateOwnUser(eq(regularUserEntity.getId()), eq(valid));
        }

        @Test
        @DisplayName("unauthenticated -> 401")
        void update_unauthenticated_401() throws Exception {
            UserRegisterRequest body = new UserRegisterRequest("any", "e@e", "StrongPass1!");

            mockMvc.perform(put("/users/me")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body, objectMapper)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /users/me")
    class DeleteMe {

        @Test
        @DisplayName("authenticated -> 200 + message")
        void delete_authenticated_ok() throws Exception {
            given(userService.deleteOwnUser(regularUserEntity.getId()))
                    .willReturn("User deleted successfully");

            mockMvc.perform(delete("/users/me")
                            .with(user(userPrincipal))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().string("User deleted successfully"));

            verify(userService, times(1)).deleteOwnUser(eq(regularUserEntity.getId()));
        }

        @Test
        @DisplayName("unauthenticated -> 401")
        void delete_unauthenticated_401() throws Exception {
            mockMvc.perform(delete("/users/me").with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /users/register")
    class RegisterUser {

        @Test
        @DisplayName("unauthenticated -> 401 (depends on SecurityConfig)")
        void register_unauthenticated_401_or_200() throws Exception {
            UserRegisterRequest req = new UserRegisterRequest("new", "new@e", "StrongPass1!");
            mockMvc.perform(post("/users/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req, objectMapper)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("authenticated USER -> 200 + body (since no role guard on method)")
        void register_authenticated_user_ok() throws Exception {
            UserRegisterRequest request = new UserRegisterRequest("new", "new@example.com", "StrongPass1!");
            UserResponse response = new UserResponse(99L, "new", "new@example.com", "ROLE_USER");
            given(userService.addUser(request)).willReturn(response);

            mockMvc.perform(post("/users/register")
                            .with(user(userPrincipal))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(toJson(response, objectMapper)));

            verify(userService, times(1)).addUser(eq(request));
        }

    @Nested
    @DisplayName("ADMIN endpoints")
    class AdminEndpoints {

        @Test
        @DisplayName("GET /users -> admin 200, user 403, unauth 401")
        void listUsers_roleChecks() throws Exception {
            List<UserResponse> all = List.of(
                    new UserResponse(1L,"admin","admin@yogi.com","ROLE_ADMIN"),
                    new UserResponse(2L,"kate","k@e","ROLE_USER")
            );
            given(userService.getAllUsers()).willReturn(all);

            mockMvc.perform(get("/users").with(user(adminPrincipal)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(toJson(all, objectMapper)));

            mockMvc.perform(get("/users").with(user(userPrincipal)))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/users"))
                    .andExpect(status().isUnauthorized());

            verify(userService, times(1)).getAllUsers();
        }

        @Test
        @DisplayName("GET /users/{id} -> admin 200, user 403, unauth 401")
        void getUserById_roleChecks() throws Exception {
            UserResponse response = new UserResponse(2L,"kate","k@e","ROLE_USER");
            given(userService.getUserByIdAdmin(2L)).willReturn(response);

            mockMvc.perform(get("/users/2").with(user(adminPrincipal)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(toJson(response, objectMapper)));

            mockMvc.perform(get("/users/2").with(user(userPrincipal)))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/users/2"))
                    .andExpect(status().isUnauthorized());

            verify(userService, times(1)).getUserByIdAdmin(eq(2L));
        }

        @Test
        @DisplayName("POST /users/admin -> admin 200, user 403, unauth 401")
        void addAdmin_roleChecks() throws Exception {
            UserRegisterRequest body = new UserRegisterRequest("anna","anna@yogi.com","StrongPass1!");
            UserResponse response = new UserResponse(50L,"anna","anna@yogi.com","ROLE_ADMIN");
            given(userService.addAdmin(body)).willReturn(response);

            mockMvc.perform(post("/users/admin")
                            .with(user(adminPrincipal))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(toJson(response, objectMapper)));

            mockMvc.perform(post("/users/admin")
                            .with(user(userPrincipal))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body, objectMapper)))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/users/admin")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body, objectMapper)))
                    .andExpect(status().isUnauthorized());

            verify(userService, times(1)).addAdmin(eq(body));
        }

        @Test
        @DisplayName("DELETE /users/{id} -> admin 200, user 403, unauth 401")
        void deleteById_roleChecks() throws Exception {
            given(userService.deleteUserByIdAdmin(5L)).willReturn("User deleted");

            mockMvc.perform(delete("/users/5")
                            .with(user(adminPrincipal))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().string("User deleted"));

            mockMvc.perform(delete("/users/5")
                            .with(user(userPrincipal))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            mockMvc.perform(delete("/users/5").with(csrf()))
                    .andExpect(status().isUnauthorized());

            verify(userService, times(1)).deleteUserByIdAdmin(eq(5L));
        }
    }
}
}