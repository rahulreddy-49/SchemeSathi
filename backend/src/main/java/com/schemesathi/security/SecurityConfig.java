package com.schemesathi.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"success\":false,\"message\":\"Authentication required\",\"data\":null}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"success\":false,\"message\":\"Access denied: insufficient permissions\",\"data\":null}");
                })
            )
            .authorizeHttpRequests(auth -> {
                auth
                    // Preflight and error endpoints
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/error").permitAll()

                    // Public authentication and demo routes
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers("/api/demo/**").permitAll();

                // H2 console only permitted if explicitly enabled
                if (h2ConsoleEnabled) {
                    auth.requestMatchers("/h2-console/**").permitAll();
                }

                auth
                    // Authenticated scheme interactions (MUST precede public scheme catalog rules)
                    .requestMatchers("/api/schemes/saved").authenticated()
                    .requestMatchers("/api/schemes/saved/**").authenticated()
                    .requestMatchers("/api/schemes/eligibility/**").authenticated()
                    .requestMatchers("/api/schemes/*/eligibility").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/schemes/*/save").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/schemes/*/save").authenticated()

                    // Public scheme browsing
                    .requestMatchers(HttpMethod.GET, "/api/schemes", "/api/schemes/").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/schemes/categories").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/schemes/{id:[0-9]+}").permitAll()

                    // Administrator-only routes
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    // All remaining endpoints require authentication
                    .anyRequest().authenticated();
            })
            .headers(headers -> {
                if (h2ConsoleEnabled) {
                    headers.frameOptions(frame -> frame.sameOrigin());
                } else {
                    headers.frameOptions(frame -> frame.deny());
                }
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:[*]",
            "http://127.0.0.1:[*]",
            "http://localhost",
            "http://127.0.0.1",
            "https://*.vercel.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
