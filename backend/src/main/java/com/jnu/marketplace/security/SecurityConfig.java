package com.jnu.marketplace.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/**",
                    "/api/public/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/manifest.json",
                    "/favicon.ico",
                    "/error",
                    "/",
                    "/index.html",
                    "/static/**",
                    "/assets/**",
                    "/*.jpg",
                    "/*.png",
                    "/*.gif",
                    "/*.ico",
                    "/*.svg",
                    "/*.css",
                    "/*.js",
                    "/jnu*.jpg",
                    "/jnu*.png"
                ).permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/listings", "/api/listings/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/listings/search").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/listings/categories").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/listings/conditions").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/listings/upload").authenticated()
                .requestMatchers("/api/messages/**").authenticated()
                .requestMatchers("/api/users/**").authenticated()
                .requestMatchers("/api/sales/**").authenticated()
                .requestMatchers("/api/sale-requests/**").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        System.out.println("Security configuration loaded successfully");
        
        // Add debugging for requests
        http.addFilterBefore(new jakarta.servlet.Filter() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, jakarta.servlet.FilterChain chain) throws java.io.IOException, jakarta.servlet.ServletException {
                jakarta.servlet.http.HttpServletRequest httpRequest = (jakarta.servlet.http.HttpServletRequest) request;
                System.out.println("Request: " + httpRequest.getMethod() + " " + httpRequest.getRequestURI());
                chain.doFilter(request, response);
            }
        }, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 