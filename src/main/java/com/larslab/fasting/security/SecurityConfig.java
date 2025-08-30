package com.larslab.fasting.security;

import com.larslab.fasting.logging.CorrelationIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final CorrelationIdFilter correlationIdFilter;
    private final SwaggerBasicAuthFilter swaggerBasicAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          RateLimitingFilter rateLimitingFilter,
                          CorrelationIdFilter correlationIdFilter,
                          SwaggerBasicAuthFilter swaggerBasicAuthFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitingFilter = rateLimitingFilter;
        this.correlationIdFilter = correlationIdFilter;
        this.swaggerBasicAuthFilter = swaggerBasicAuthFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/health", "/actuator/info",
                    "/api/users/login-or-create", "/api/users/refresh", "/api/users/logout",
                    // allow swagger endpoints through authorization; they are gated by SwaggerBasicAuthFilter
                    "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(swaggerBasicAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(rateLimitingFilter, JwtAuthenticationFilter.class);
        return http.build();
    }
}
