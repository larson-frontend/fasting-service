package com.larslab.fasting.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final com.larslab.fasting.logging.CorrelationIdFilter correlationIdFilter;
    private final Environment environment;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService,
                          RateLimitingFilter rateLimitingFilter,
                          com.larslab.fasting.logging.CorrelationIdFilter correlationIdFilter,
                          Environment environment) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.rateLimitingFilter = rateLimitingFilter;
        this.correlationIdFilter = correlationIdFilter;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> {}) // Use default CORS configuration from CorsConfig
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'; form-action 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; connect-src 'self'"))
                .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                .frameOptions(frame -> frame.deny())
            )
            .authorizeHttpRequests(authz -> {
                boolean prod = isProd();
                authz
                // Public endpoints - no authentication required
                .requestMatchers("/api/users/login-or-create", "/api/users/check-availability", "/api/users/refresh", "/api/users/logout").permitAll()
                // API Documentation endpoints - restrict in prod (require auth)
                .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .access((authentication, context) -> new org.springframework.security.authorization.AuthorizationDecision(!prod || (authentication != null && authentication.get().isAuthenticated())))
                // Actuator health endpoint - public access
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/fast/status", "/api/fast/history").authenticated()
                .requestMatchers("/api/fast/**").authenticated()
                .requestMatchers("/api/users/**").authenticated()
                .anyRequest().authenticated();
            })
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Add JWT filter first so we can reliably insert rate limiting before it
            .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    private boolean isProd() {
        for (String profile : environment.getActiveProfiles()) {
            if (profile.equalsIgnoreCase("prod") || profile.equalsIgnoreCase("production")) {
                return true;
            }
        }
        return false;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
