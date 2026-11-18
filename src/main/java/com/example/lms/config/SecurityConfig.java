package com.example.lms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String[] COURSE_CONTENT = {
            "/api/courses/**", "/api/lessons/**", "/api/assignments/**",
            "/api/quizzes/**", "/api/questions/**", "/api/categories/**", "/api/tags/**"
    };

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/submissions", "/api/quiz-submissions")
                        .hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/submissions/**", "/api/quiz-submissions/**")
                        .hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                        .requestMatchers("/api/submissions/**", "/api/quiz-submissions/**")
                        .hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, COURSE_CONTENT).hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, COURSE_CONTENT).hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, COURSE_CONTENT).hasAnyRole("TEACHER", "ADMIN")
                        .anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    UserDetailsService users(@Value("${lms.demo.admin-password}") String admin,
                             @Value("${lms.demo.teacher-password}") String teacher,
                             @Value("${lms.demo.student-password}") String student) {
        InMemoryUserDetailsManager users = new InMemoryUserDetailsManager();
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        addDemoUser(users, encoder, "admin", admin, "ADMIN");
        addDemoUser(users, encoder, "teacher", teacher, "TEACHER");
        addDemoUser(users, encoder, "student", student, "STUDENT");
        return users;
    }

    private void addDemoUser(InMemoryUserDetailsManager users, PasswordEncoder encoder,
                             String username, String password, String role) {
        if (!password.isBlank()) {
            users.createUser(User.withUsername(username).password(encoder.encode(password)).roles(role).build());
        }
    }
}
