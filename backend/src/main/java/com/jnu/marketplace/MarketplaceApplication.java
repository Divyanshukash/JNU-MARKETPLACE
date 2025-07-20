package com.jnu.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

/**
 * Main Spring Boot application class for JNU Marketplace
 * 
 * This application provides a comprehensive marketplace platform for the JNU community,
 * enabling students, faculty, and staff to buy and sell items and services within the campus.
 * 
 * Features include:
 * - User authentication and registration
 * - Item/service listings with multiple images
 * - Messaging system for buyer-seller communication
 * - Advanced search and filtering
 * - Wishlist and favorites functionality
 * 
 * @author JNU Marketplace Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableMongoAuditing
@EnableAsync
@EnableScheduling
public class MarketplaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketplaceApplication.class, args);
    }

    // Add this bean to serve files from /uploads/
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations("file:uploads/");
            }
        };
    }
} 