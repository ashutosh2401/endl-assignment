package com.assignment.user_fa.scheduler;

import com.assignment.user_fa.model.Session;
import com.assignment.user_fa.model.SessionHistory;
import com.assignment.user_fa.repository.SessionHistoryRepository;
import com.assignment.user_fa.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SessionScheduler {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SessionHistoryRepository historyRepository;

    /**
     * Scheduled task that runs every 5 minutes (300000 ms).
     * It checks for all expired sessions (where expiresAt < current time),
     * moves them to session history, and deletes them from the active session table.
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void purgeInactiveSessions() {
        System.out.println("Purge Inactive sessions scheduler running.");
        // Fetch all sessions that have expired
        List<Session> expired = sessionRepository.findByExpiresAtBefore(LocalDateTime.now());

        // Archive each expired session and then delete it from the active session store
        for (Session session : expired) {
            System.out.println("expired session : " + session);
            historyRepository.save(new SessionHistory(session));
            sessionRepository.delete(session);
        }
    }
}
