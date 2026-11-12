package com.flashcards.security;

import com.flashcards.common.security.JwtVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Wires token checking into the deck service: creates the shared JwtVerifier from
// the configured secret and puts JwtAuthFilter in front of every /api request.
//
// The filter is registered here rather than marked @Component, so controller
// tests (@WebMvcTest) don't load it; it has its own tests in JwtAuthFilterTest.
@Configuration
public class SecurityConfig {

    @Bean
    public JwtVerifier jwtVerifier(@Value("${jwt.secret}") String secret) {
        return new JwtVerifier(secret);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilter(JwtVerifier jwtVerifier) {
        FilterRegistrationBean<JwtAuthFilter> registration =
                new FilterRegistrationBean<>(new JwtAuthFilter(jwtVerifier));
        registration.addUrlPatterns("/api/*");
        return registration;
    }
}
