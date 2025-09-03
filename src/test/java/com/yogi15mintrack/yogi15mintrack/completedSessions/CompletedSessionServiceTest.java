package com.yogi15mintrack.yogi15mintrack.completedSessions;

import com.yogi15mintrack.yogi15mintrack.completedSessions.dto.CompletedCreateRequest;
import com.yogi15mintrack.yogi15mintrack.completedSessions.dto.CompletedResponse;
import com.yogi15mintrack.yogi15mintrack.sessions.Session;
import com.yogi15mintrack.yogi15mintrack.sessions.SessionRepository;
import com.yogi15mintrack.yogi15mintrack.users.Role;
import com.yogi15mintrack.yogi15mintrack.users.User;
import com.yogi15mintrack.yogi15mintrack.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class CompletedSessionServiceTest {

    @Mock
    private CompletedSessionRepository completedSessionRepository;
    @Mock private UserRepository userRepository;
    @Mock private SessionRepository sessionRepository;

    @InjectMocks
    private CompletedSessionService completedSessionService;

    private User testUser;
    private Session testSession;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("kate")
                .email("k@e")
                .password("p")
                .role(Role.USER)
                .build();

        testSession = Session.builder()
                .id(7L)
                .title("Day 1")
                .build();
    }

    @Test
    void completeToday_happyPath_createsAndReturnsDto() {
        LocalDate today = LocalDate.now();
        CompletedCreateRequest createRequest =
                new CompletedCreateRequest(testSession.getId(), Mood.GOOD, "note");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(sessionRepository.findById(7L)).thenReturn(Optional.of(testSession));
        when(completedSessionRepository.existsByUserIdAndDateCompleted(1L, today)).thenReturn(false);

        when(completedSessionRepository.save(any(CompletedSession.class)))
                .thenAnswer(invocation -> {
                    CompletedSession completedSession = invocation.getArgument(0);
                    completedSession.setId(100L);
                    return completedSession;
                });

        CompletedResponse completedResponse = completedSessionService.completeToday(1L, createRequest);

        ArgumentCaptor<CompletedSession> sessionCaptor = ArgumentCaptor.forClass(CompletedSession.class);
        verify(completedSessionRepository).save(sessionCaptor.capture());
        CompletedSession savedSession = sessionCaptor.getValue();

        assertThat(savedSession.getUser().getId()).isEqualTo(1L);
        assertThat(savedSession.getSession().getId()).isEqualTo(7L);
        assertThat(savedSession.getDateCompleted()).isEqualTo(today);
        assertThat(savedSession.getState()).isEqualTo(Mood.GOOD);
        assertThat(savedSession.getNote()).isEqualTo("note");

        assertThat(completedResponse.id()).isEqualTo(100L);
        assertThat(completedResponse.sessionId()).isEqualTo(7L);
        assertThat(completedResponse.sessionTitle()).isEqualTo("Day 1");
        assertThat(completedResponse.dateCompleted()).isEqualTo(today);
        assertThat(completedResponse.state()).isEqualTo(Mood.GOOD);
        assertThat(completedResponse.note()).isEqualTo("note");
    }

    @Test
    void completeToday_secondTimeSameDay_throwsIllegalState() {
        LocalDate today = LocalDate.now();
        CompletedCreateRequest createRequest =
                new CompletedCreateRequest(7L, Mood.OKAY, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(sessionRepository.findById(7L)).thenReturn(Optional.of(testSession));
        when(completedSessionRepository.existsByUserIdAndDateCompleted(1L, today)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> completedSessionService.completeToday(1L, createRequest));
        verify(completedSessionRepository, never()).save(any());
    }

    @Test
    void isCompletedToday_trueFalse() {
        when(completedSessionRepository.existsByUserIdAndDateCompleted(1L, LocalDate.now())).thenReturn(true);
        assertThat(completedSessionService.isCompletedToday(1L)).isTrue();

        when(completedSessionRepository.existsByUserIdAndDateCompleted(2L, LocalDate.now())).thenReturn(false);
        assertThat(completedSessionService.isCompletedToday(2L)).isFalse();
    }

    @Test
    void myCompleted_returnsDescendingList() {
        LocalDate today = LocalDate.now();

        CompletedSession completedSessionOne = CompletedSession.builder()
                .id(1L).user(testUser).session(testSession).dateCompleted(today).state(Mood.GOOD).build();
        CompletedSession completedSessionTwo = CompletedSession.builder()
                .id(2L).user(testUser).session(testSession).dateCompleted(today).state(Mood.GOOD).build();

        when(completedSessionRepository.findByUserIdOrderByDateCompletedDesc(1L))
                .thenReturn(List.of(completedSessionOne, completedSessionTwo));

        List<CompletedResponse> completedResponses = completedSessionService.myCompleted(1L);

        assertThat(completedResponses).hasSize(2);
        assertThat(completedResponses.get(0).dateCompleted()).isEqualTo(today);
    }

    @Test
    void currentStreak_zeroWhenNotCompletedToday() {
        when(completedSessionRepository.existsByUserIdAndDateCompleted(1L, LocalDate.now())).thenReturn(false);
        assertThat(completedSessionService.currentStreak(1L)).isZero();
    }

    @Test
    void currentStreak_countsConsecutiveDays() {
        LocalDate today = LocalDate.now();
        when(completedSessionRepository.existsByUserIdAndDateCompleted(1L, today)).thenReturn(true);
        when(completedSessionRepository.existsByUserIdAndDateCompleted(1L, today.minusDays(1))).thenReturn(true);
        when(completedSessionRepository.existsByUserIdAndDateCompleted(1L, today.minusDays(2))).thenReturn(true);
        when(completedSessionRepository.existsByUserIdAndDateCompleted(1L, today.minusDays(3))).thenReturn(false);

        assertThat(completedSessionService.currentStreak(1L)).isEqualTo(3);
    }
}