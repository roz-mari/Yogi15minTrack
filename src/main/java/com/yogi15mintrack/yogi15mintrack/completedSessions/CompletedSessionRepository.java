package com.yogi15mintrack.yogi15mintrack.completedSessions;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CompletedSessionRepository extends JpaRepository<CompletedSession, Long> {

    List<CompletedSession> findByUserIdOrderByDateCompletedDesc(Long userId);

    Optional<CompletedSession> findByUserIdAndDateCompleted(Long userId, LocalDate date);

    boolean existsByUserIdAndDateCompleted(Long userId, LocalDate date);
}
