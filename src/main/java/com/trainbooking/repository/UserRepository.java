package com.trainbooking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trainbooking.entity.Role;
import com.trainbooking.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<User> findByRoleOrderByCreatedAtDesc(Role role);

    long countByRoleAndActiveTrue(Role role);
}