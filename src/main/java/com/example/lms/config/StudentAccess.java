package com.example.lms.config;

import com.example.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class StudentAccess {

    private final UserRepository users;
    private final String studentEmail;

    public StudentAccess(UserRepository users, @Value("${lms.demo.student-email}") String studentEmail) {
        this.users = users;
        this.studentEmail = studentEmail;
    }

    public void requireAccess(Authentication authentication, Long studentId) {
        if (authentication == null || !"student".equals(authentication.getName())) {
            return;
        }
        if (studentId == null || users.findByEmail(studentEmail)
                .filter(user -> user.getId().equals(studentId)).isEmpty()) {
            throw new AccessDeniedException("Students may only access their own work");
        }
    }
}
