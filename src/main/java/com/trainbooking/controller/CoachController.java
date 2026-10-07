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

import com.trainbooking.dto.CoachRequest;
import com.trainbooking.dto.CoachResponse;
import com.trainbooking.service.CoachService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class CoachController {

    private final CoachService coachService;

    public CoachController(
            CoachService coachService) {

        this.coachService = coachService;
    }

    @PostMapping("/trains/{trainId}/coaches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CoachResponse> createCoach(
            @PathVariable Long trainId,
            @Valid @RequestBody CoachRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(coachService.createCoach(
                        trainId,
                        request,
                        authentication.getName()));
    }

    @GetMapping("/trains/{trainId}/coaches")
    public ResponseEntity<List<CoachResponse>> getCoaches(
            @PathVariable Long trainId) {

        return ResponseEntity.ok(
                coachService.getCoachesByTrain(trainId));
    }

    @GetMapping("/coaches/{coachId}")
    public ResponseEntity<CoachResponse> getCoach(
            @PathVariable Long coachId) {

        return ResponseEntity.ok(
                coachService.getCoachById(coachId));
    }

    @PutMapping("/coaches/{coachId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CoachResponse> updateCoach(
            @PathVariable Long coachId,
            @Valid @RequestBody CoachRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                coachService.updateCoach(
                        coachId,
                        request,
                        authentication.getName()));
    }

    @DeleteMapping("/coaches/{coachId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateCoach(
            @PathVariable Long coachId,
            Authentication authentication) {

        coachService.deactivateCoach(
                coachId,
                authentication.getName());

        return ResponseEntity.noContent().build();
    }
}