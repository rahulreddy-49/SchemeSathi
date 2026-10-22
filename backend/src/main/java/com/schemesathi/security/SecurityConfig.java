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

import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Environment env;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, Environment env) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.env = env;
    }

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173,http://127.0.0.1:3000}")
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

                    // Public authentication, health check, and demo routes
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/public/health").permitAll()
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
                    .requestMatchers("/api/applications", "/api/applications/**").authenticated()

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

        boolean isProduction = env != null && (
                env.matchesProfiles("prod", "production")
                || "prod".equalsIgnoreCase(env.getProperty("app.environment"))
                || "production".equalsIgnoreCase(env.getProperty("app.environment"))
        );

        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(s -> !isProduction || (!s.contains("localhost") && !s.contains("127.0.0.1")))
                .toList();

        if (!origins.isEmpty()) {
            configuration.setAllowedOrigins(origins);
        }

        // Localhost and 127.0.0.1 patterns are strictly permitted in non-production environments only
        if (!isProduction) {
            configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:[*]",
                "http://127.0.0.1:[*]"
            ));
        }

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
