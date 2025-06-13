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

    /**
     * This filter intercepts every HTTP request once per request,
     * records metadata such as endpoint, method, response code, IP address, and duration,
     * and saves it to the audit log repository.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis(); // Capture start time before processing the request

        // Continue processing the request
        filterChain.doFilter(request, response);

        long duration = System.currentTimeMillis() - start; // Calculate request handling duration

        // Create a new audit log entry
        AuditLog log = new AuditLog();
        log.setEndpoint(request.getRequestURI());           // API endpoint being accessed
        log.setMethod(request.getMethod());                 // HTTP method (GET, POST, etc.)
        log.setResponseCode(response.getStatus());          // HTTP response status code
        log.setStatus(response.getStatus() >= 400 ? "FAILED" : "SUCCESS"); // Mark as SUCCESS or FAILED based on status
        log.setTimestamp(LocalDateTime.now());              // Current timestamp
        log.setIpAddress(request.getRemoteAddr());          // IP address of the client
        log.setDuration(duration);                          // Total time taken to process the request

        auditLogRepository.save(log);
    }
}
