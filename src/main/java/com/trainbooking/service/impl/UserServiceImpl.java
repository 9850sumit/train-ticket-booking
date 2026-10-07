package com.trainbooking.service.impl;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trainbooking.dto.AdminCreateRequest;
import com.trainbooking.dto.AdminResponse;
import com.trainbooking.dto.AdminUpdateRequest;
import com.trainbooking.dto.RegisterRequest;
import com.trainbooking.dto.UserResponse;
import com.trainbooking.entity.Role;
import com.trainbooking.entity.User;
import com.trainbooking.exception.DuplicateResourceException;
import com.trainbooking.exception.ResourceNotFoundException;
import com.trainbooking.repository.UserRepository;
import com.trainbooking.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {

        String email = normalizeEmail(request.getEmail());
        String phone = request.getPhone().trim();
        String fullName = request.getFullName().trim();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(
                    "Email already registered"
            );
        }

        if (userRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException(
                    "Phone number already registered"
            );
        }

        User user = new User();

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(Role.USER);
        user.setActive(true);

        try {
            User savedUser = userRepository.save(user);

            return new UserResponse(
                    savedUser.getId(),
                    savedUser.getFullName(),
                    savedUser.getEmail(),
                    savedUser.getPhone(),
                    savedUser.getRole(),
                    savedUser.getActive(),
                    savedUser.getCreatedAt(),
                    savedUser.getUpdatedAt()
            );

        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException(
                    "Email or phone number is already registered"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminResponse> getAdmins() {

        return userRepository
                .findByRoleOrderByCreatedAtDesc(Role.ADMIN)
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }

    @Override
    @Transactional
    public AdminResponse createAdmin(AdminCreateRequest request) {

        String email = normalizeEmail(request.getEmail());
        String phone = request.getPhone().trim();
        String fullName = request.getFullName().trim();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(
                    "Email already registered"
            );
        }

        if (userRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException(
                    "Phone number already registered"
            );
        }

        User admin = new User();

        admin.setFullName(fullName);
        admin.setEmail(email);
        admin.setPhone(phone);
        admin.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        admin.setRole(Role.ADMIN);
        admin.setActive(true);

        try {
            User savedAdmin = userRepository.save(admin);

            return toAdminResponse(savedAdmin);

        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException(
                    "Email or phone number is already registered"
            );
        }
    }

    @Override
    @Transactional
    public AdminResponse updateAdmin(
            Long adminId,
            AdminUpdateRequest request) {

        User admin = userRepository.findById(adminId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Administrator not found"
                        )
                );

        if (admin.getRole() != Role.ADMIN) {
            throw new ResourceNotFoundException(
                    "Administrator not found"
            );
        }

        String phone = request.getPhone().trim();

        if (!phone.equals(admin.getPhone())
                && userRepository.existsByPhone(phone)) {

            throw new DuplicateResourceException(
                    "Phone number already registered"
            );
        }

        admin.setFullName(request.getFullName().trim());
        admin.setPhone(phone);

        try {
            User savedAdmin = userRepository.save(admin);

            return toAdminResponse(savedAdmin);

        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException(
                    "Phone number is already registered"
            );
        }
    }

    @Override
    @Transactional
    public void deactivateAdmin(
            Long adminId,
            String requesterEmail) {

        User admin = userRepository.findById(adminId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Administrator not found"
                        )
                );

        if (admin.getRole() != Role.ADMIN) {
            throw new ResourceNotFoundException(
                    "Administrator not found"
            );
        }

        if (!Boolean.TRUE.equals(admin.getActive())) {
            throw new IllegalStateException(
                    "Administrator is already inactive"
            );
        }

        String normalizedRequesterEmail =
                normalizeEmail(requesterEmail);

        if (admin.getEmail().equals(normalizedRequesterEmail)) {
            throw new IllegalStateException(
                    "You cannot deactivate your own administrator account"
            );
        }

        long activeAdminCount =
                userRepository.countByRoleAndActiveTrue(Role.ADMIN);

        if (activeAdminCount <= 1) {
            throw new IllegalStateException(
                    "At least one active administrator must remain"
            );
        }

        admin.setActive(false);

        userRepository.save(admin);
    }

    private AdminResponse toAdminResponse(User admin) {

        return new AdminResponse(
                admin.getId(),
                admin.getFullName(),
                admin.getEmail(),
                admin.getPhone(),
                admin.getRole(),
                admin.getActive(),
                admin.getCreatedAt(),
                admin.getUpdatedAt()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}