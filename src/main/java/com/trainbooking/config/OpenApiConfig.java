package com.trainbooking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI trainTicketBookingOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Train Ticket Booking System API")
                        .version("1.0.0")
                        .description(
                                "REST API for the Train Ticket Booking System"
                        )
                        .contact(new Contact()
                                .name("Train Ticket Booking Team")))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }
}