package com.assignment.user_fa.filter;

import com.assignment.user_fa.model.AuditLog;
import com.assignment.user_fa.repository.AuditLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class ApiRequestLoggingFilter extends OncePerRequestFilter {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();
        filterChain.doFilter(request, response);
        long duration = System.currentTimeMillis() - start;

        AuditLog log = new AuditLog();
        log.setEndpoint(request.getRequestURI());
        log.setMethod(request.getMethod());
        log.setResponseCode(response.getStatus());
        log.setStatus(response.getStatus() >= 400 ? "FAILED" : "SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        log.setIpAddress(request.getRemoteAddr());
        log.setDuration(duration);
        auditLogRepository.save(log);
    }
}
