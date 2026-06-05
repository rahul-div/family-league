package com.familyleague.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          UserDetailsServiceImpl userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setStatus(403);
                            response.getWriter().write("{\"timestamp\":\"" + java.time.Instant.now() + "\",\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied\",\"path\":\"" + request.getRequestURI() + "\"}");
                        }))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // Admin-only endpoints
                        .requestMatchers(HttpMethod.POST, "/auth/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/leagues", "/leagues/").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/leagues/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/leagues/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/seasons", "/seasons/").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/seasons/*/open", "/seasons/*/close").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/seasons/*/teams").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/seasons/*/teams/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/seasons/*/publish-result").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/teams", "/teams/").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/teams/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/teams/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/players", "/players/").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/players/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/players/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/matches", "/matches/").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/matches/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/matches/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/results/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/leaderboard/*/recalculate").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/notifications/bulk").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/notifications").hasRole("ADMIN")
                        .requestMatchers("/config", "/config/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
