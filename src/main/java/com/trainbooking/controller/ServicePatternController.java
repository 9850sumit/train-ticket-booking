package com.trainbooking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trainbooking.dto.ServicePatternRequest;
import com.trainbooking.dto.ServicePatternResponse;
import com.trainbooking.service.ServicePatternService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ServicePatternController {

    private final ServicePatternService servicePatternService;

    public ServicePatternController(
            ServicePatternService servicePatternService) {
        this.servicePatternService = servicePatternService;
    }

    @PostMapping("/trains/{trainId}/service-patterns")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicePatternResponse> createPattern(
            @PathVariable Long trainId,
            @Valid @RequestBody ServicePatternRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servicePatternService.createPattern(
                        trainId,
                        request,
                        authentication.getName()));
    }

    @GetMapping("/service-patterns/my")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServicePatternResponse>> getMyPatterns(
            Authentication authentication) {

        return ResponseEntity.ok(
                servicePatternService.getMyPatterns(
                        authentication.getName()));
    }

    @GetMapping("/trains/{trainId}/service-patterns")
    public ResponseEntity<List<ServicePatternResponse>> getTrainPatterns(
            @PathVariable Long trainId) {

        return ResponseEntity.ok(
                servicePatternService.getTrainPatterns(trainId));
    }

    @GetMapping("/service-patterns/{patternId}")
    public ResponseEntity<ServicePatternResponse> getPattern(
            @PathVariable Long patternId) {

        return ResponseEntity.ok(
                servicePatternService.getPatternById(patternId));
    }

    @PutMapping("/service-patterns/{patternId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicePatternResponse> updatePattern(
            @PathVariable Long patternId,
            @Valid @RequestBody ServicePatternRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                servicePatternService.updatePattern(
                        patternId,
                        request,
                        authentication.getName()));
    }

    @DeleteMapping("/service-patterns/{patternId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivatePattern(
            @PathVariable Long patternId,
            Authentication authentication) {

        servicePatternService.deactivatePattern(
                patternId,
                authentication.getName());

        return ResponseEntity.noContent().build();
    }
}