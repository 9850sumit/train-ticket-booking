package com.trainbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.trainbooking.security.JwtProperties;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class TrainTicketBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainTicketBookingApplication.class, args);
    }
}