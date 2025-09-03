package com.yogi15mintrack.yogi15mintrack.sessions;

import com.yogi15mintrack.yogi15mintrack.sessions.dto.SessionCreateRequest;
import com.yogi15mintrack.yogi15mintrack.sessions.dto.SessionResponse;
import com.yogi15mintrack.yogi15mintrack.sessions.dto.SessionUpdateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private SessionService sessionService;

    private Session existing;

    @BeforeEach
    void setUp() {
        existing = Session.builder()
                .id(10L)
                .title("Morning Flow")
                .description("Gentle start")
                .videoUrl("http://video")
                .dayOrder(1)
                .build();
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(sessionRepository);
    }

    @Test
    void getAllSessions_returnsMappedList() {
        when(sessionRepository.findAll()).thenReturn(List.of(existing));

        List<SessionResponse> result = sessionService.getAllSessions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getTitle()).isEqualTo("Morning Flow");
        assertThat(result.get(0).getDescription()).isEqualTo("Gentle start");
        assertThat(result.get(0).getVideoUrl()).isEqualTo("http://video");
        assertThat(result.get(0).getDayOrder()).isEqualTo(1);
        verify(sessionRepository).findAll();
    }

    @Test
    void getTodaySession_whenExists_returnsDto() {
        int today = LocalDate.now().getDayOfWeek().getValue();
        when(sessionRepository.findByDayOrder(today)).thenReturn(Optional.of(existing));

        SessionResponse result = sessionService.getTodaySession();

        assertThat(result.getId()).isEqualTo(existing.getId());
        assertThat(result.getTitle()).isEqualTo(existing.getTitle());
        verify(sessionRepository).findByDayOrder(today);
    }

    @Test
    void getTodaySession_whenMissing_throws() {
        int today = LocalDate.now().getDayOfWeek().getValue();
        when(sessionRepository.findByDayOrder(today)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> sessionService.getTodaySession());
        verify(sessionRepository).findByDayOrder(today);
    }

    @Test
    void createSession_persistsAndReturnsResponse() {
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> {
            Session saved = inv.getArgument(0);
            saved.setId(11L);
            return saved;
        });

        SessionCreateRequest request = new SessionCreateRequest(
                "New Session", "Description", "http://url", 2
        );

        SessionResponse created = sessionService.createSession(request);

        assertThat(created.getId()).isEqualTo(11L);
        assertThat(created.getTitle()).isEqualTo("New Session");
        assertThat(created.getDescription()).isEqualTo("Description");
        assertThat(created.getVideoUrl()).isEqualTo("http://url");
        assertThat(created.getDayOrder()).isEqualTo(2);
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void updateSession_whenFound_updatesAndReturnsResponse() {
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(sessionRepository.save(any(Session.class))).thenReturn(existing);

        SessionUpdateRequest request = new SessionUpdateRequest(
                "Updated", "Updated description", "http://new", 3
        );

        SessionResponse updated = sessionService.updateSession(10L, request);

        assertThat(updated.getTitle()).isEqualTo("Updated");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getVideoUrl()).isEqualTo("http://new");
        assertThat(updated.getDayOrder()).isEqualTo(3);
        verify(sessionRepository).findById(10L);
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void updateSession_whenMissing_throws() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());
        SessionUpdateRequest request = new SessionUpdateRequest("t", "d", "u", 1);

        assertThrows(RuntimeException.class, () -> sessionService.updateSession(99L, request));
        verify(sessionRepository).findById(99L);
    }

    @Test
    void deleteSession_whenExists_deletes() {
        when(sessionRepository.existsById(10L)).thenReturn(true);

        sessionService.deleteSession(10L);

        verify(sessionRepository).existsById(10L);
        verify(sessionRepository).deleteById(10L);
    }

    @Test
    void deleteSession_whenMissing_throws() {
        when(sessionRepository.existsById(10L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> sessionService.deleteSession(10L));
        verify(sessionRepository).existsById(10L);
    }
}