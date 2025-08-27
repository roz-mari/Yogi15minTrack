package com.yogi15mintrack.yogi15mintrack.sessions;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByDayOrder(int dayOrder);
}
