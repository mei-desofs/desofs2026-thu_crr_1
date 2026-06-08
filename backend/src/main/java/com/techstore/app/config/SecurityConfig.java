package com.techstore.app.config;

import com.techstore.app.config.jwt.JWTAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for the application.
 * This class sets up the security filter chain, disabling CSRF protection and
 * configuring session management to be stateless.
 * It also defines which endpoints are publicly accessible and which require
 * authentication.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JWTAuthFilter jwtAuthFilter;

    public SecurityConfig(JWTAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/index.html", "/reset-password.html", "/auth/login", "/auth/refresh",
                                "/auth/callback", "/auth/register",
                                "/auth/confirm", "/auth/confirm-invite", "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-ui.html", "/actuator/health", "/auth/password-reset/request")
                        .permitAll()

                        .requestMatchers("/auth/invite").hasRole("MANAGER")
                        .requestMatchers("/auth/logout").hasAnyRole("MANAGER", "CUSTOMER", "CARRIER")
                        .requestMatchers("/auth/me").hasAnyRole("MANAGER", "CUSTOMER", "CARRIER")

                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers("/backup/**").hasRole("MANAGER")

                        .requestMatchers(HttpMethod.POST, "/products/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/products/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/products/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("MANAGER")

                        .requestMatchers(HttpMethod.GET, "/categories/**").hasRole("MANAGER")

                        .requestMatchers("/cart/items").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/orders").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/orders").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/orders/carrier").hasRole("CARRIER")
                        .requestMatchers(HttpMethod.PATCH, "/orders/*/pickup").hasRole("CARRIER")
                        .requestMatchers(HttpMethod.GET, "/orders/pending").hasRole("CARRIER")
                        .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:8081",
                "https://techstore.francecentral.cloudapp.azure.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
