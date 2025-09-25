package com.yogi15mintrack.yogi15mintrack.sessions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yogi15mintrack.yogi15mintrack.security.CustomUserDetail;
import com.yogi15mintrack.yogi15mintrack.sessions.dto.SessionCreateRequest;
import com.yogi15mintrack.yogi15mintrack.sessions.dto.SessionResponse;
import com.yogi15mintrack.yogi15mintrack.sessions.dto.SessionUpdateRequest;
import com.yogi15mintrack.yogi15mintrack.users.Role;
import com.yogi15mintrack.yogi15mintrack.users.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SessionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private SessionService sessionService;

    private SessionResponse sessionResponse;

    private CustomUserDetail adminPrincipal;
    private CustomUserDetail userPrincipal;

    @BeforeEach
    void setUp() {
        sessionResponse = new SessionResponse(
                1L, "Morning Yoga", "Start your day right", "http://video-url", 1
        );

        User admin = User.builder()
                .id(100L).username("admin").email("admin@yogi.com")
                .password("x").role(Role.ADMIN).build();
        User userEntity = User.builder()
                .id(200L).username("kate").email("k@e")
                .password("x").role(Role.USER).build();

        adminPrincipal = new CustomUserDetail(admin);
        userPrincipal  = new CustomUserDetail(userEntity);
    }

    private String asJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("GET /sessions -> 200 OK with body (authenticated)")
    void getAllSessions_ok_auth() throws Exception {
        given(sessionService.getAllSessions()).willReturn(List.of(sessionResponse));

        mockMvc.perform(get("/sessions").with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(content().json(asJson(List.of(sessionResponse))));
    }

    @Test
    @DisplayName("GET /sessions -> 401 when unauthenticated")
    void getAllSessions_unauth() throws Exception {
        mockMvc.perform(get("/sessions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /sessions/today -> 200 OK with today's session (authenticated)")
    void getToday_ok_auth() throws Exception {
        given(sessionService.getTodaySession()).willReturn(sessionResponse);

        mockMvc.perform(get("/sessions/today").with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(content().json(asJson(sessionResponse)));
    }

    @Test
    @DisplayName("GET /sessions/today -> 401 when unauthenticated")
    void getToday_unauth() throws Exception {
        mockMvc.perform(get("/sessions/today"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /sessions -> 200 OK with body (ADMIN)")
    void create_admin_ok() throws Exception {
        SessionCreateRequest request = new SessionCreateRequest(
                "Morning Yoga", "Start your day", "http://video-url", 1
        );
        given(sessionService.createSession(request)).willReturn(sessionResponse);

        mockMvc.perform(post("/sessions")
                        .with(user(adminPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(asJson(sessionResponse)));

        verify(sessionService, times(1)).createSession(request);
    }

    @Test
    @DisplayName("POST /sessions -> 403 Forbidden for USER (no ADMIN role)")
    void create_user_forbidden() throws Exception {
        SessionCreateRequest request = new SessionCreateRequest(
                "Morning Yoga", "Start your day", "http://video-url", 1
        );

        mockMvc.perform(post("/sessions")
                        .with(user(userPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /sessions -> 401 when unauthenticated")
    void create_unauth() throws Exception {
        SessionCreateRequest request = new SessionCreateRequest(
                "Morning Yoga", "Start your day", "http://video-url", 1
        );

        mockMvc.perform(post("/sessions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /sessions/{id} -> 200 OK with body (ADMIN)")
    void update_admin_ok() throws Exception {
        SessionUpdateRequest request = new SessionUpdateRequest(
                "Updated Title", "Updated Desc", "http://updated", 3
        );
        given(sessionService.updateSession(1L, request)).willReturn(sessionResponse);

        mockMvc.perform(put("/sessions/1")
                        .with(user(adminPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(asJson(sessionResponse)));

        verify(sessionService, times(1)).updateSession(1L, request);
    }

    @Test
    @DisplayName("PUT /sessions/{id} -> 403 Forbidden for USER")
    void update_user_forbidden() throws Exception {
        SessionUpdateRequest request = new SessionUpdateRequest(
                "Updated Title", "Updated Desc", "http://updated", 3
        );

        mockMvc.perform(put("/sessions/1")
                        .with(user(userPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /sessions/{id} -> 401 when unauthenticated")
    void update_unauth() throws Exception {
        SessionUpdateRequest request = new SessionUpdateRequest(
                "Updated Title", "Updated Desc", "http://updated", 3
        );

        mockMvc.perform(put("/sessions/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /sessions/{id} -> 204 No Content (ADMIN)")
    void delete_admin_ok() throws Exception {
        mockMvc.perform(delete("/sessions/1")
                        .with(user(adminPrincipal))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(sessionService, times(1)).deleteSession(1L);
    }

    @Test
    @DisplayName("DELETE /sessions/{id} -> 403 Forbidden for USER")
    void delete_user_forbidden() throws Exception {
        mockMvc.perform(delete("/sessions/1")
                        .with(user(userPrincipal))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /sessions/{id} -> 401 when unauthenticated")
    void delete_unauth() throws Exception {
        mockMvc.perform(delete("/sessions/1").with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}