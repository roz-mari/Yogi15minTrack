package com.yogi15mintrack.yogi15mintrack.completedSessions;

import com.yogi15mintrack.yogi15mintrack.sessions.Session;
import com.yogi15mintrack.yogi15mintrack.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "completed_sessions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date_completed"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompletedSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(name = "date_completed", nullable = false)
    private LocalDate dateCompleted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mood state;;

    @Column(columnDefinition = "TEXT")
    private String note;
}
