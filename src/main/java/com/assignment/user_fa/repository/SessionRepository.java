package com.assignment.user_fa.repository;

import com.assignment.user_fa.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByToken(String token);
    List<Session> findByExpiresAtBefore(LocalDateTime time);
}
