package com.trainbooking.controller;

import java.util.List;
import java.util.Map;

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

import com.trainbooking.dto.AdminCreateRequest;
import com.trainbooking.dto.AdminResponse;
import com.trainbooking.dto.AdminUpdateRequest;
import com.trainbooking.dto.UserResponse;
import com.trainbooking.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(
            Authentication authentication) {

        return Map.of(
                "message", "Authenticated successfully",
                "email", authentication.getName(),
                "authorities", authentication.getAuthorities()
        );
    }

    @GetMapping("/admin-test")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> adminTest() {

        return Map.of(
                "message", "Admin authorization successful"
        );
    }

    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminResponse>> getAdmins() {

        return ResponseEntity.ok(
                userService.getAdmins()
        );
    }

    @PostMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminResponse> createAdmin(
            @Valid @RequestBody AdminCreateRequest request) {

        AdminResponse response =
                userService.createAdmin(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/admins/{adminId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminResponse> updateAdmin(
            @PathVariable Long adminId,
            @Valid @RequestBody AdminUpdateRequest request) {

        return ResponseEntity.ok(
                userService.updateAdmin(
                        adminId,
                        request
                )
        );
    }

    @DeleteMapping("/admins/{adminId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateAdmin(
            @PathVariable Long adminId,
            Authentication authentication) {

        userService.deactivateAdmin(
                adminId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}