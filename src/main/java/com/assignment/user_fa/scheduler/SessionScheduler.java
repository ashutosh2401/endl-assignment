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

    @Scheduled(fixedRate = 300000) // every 5 min
    public void purgeInactiveSessions() {
        List<Session> expired = sessionRepository.findByExpiresAtBefore(LocalDateTime.now());
        for (Session session : expired) {
            historyRepository.save(new SessionHistory(session));
            sessionRepository.delete(session);
        }
    }
}
