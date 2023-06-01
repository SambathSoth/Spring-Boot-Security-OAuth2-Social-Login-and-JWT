package com.sambath.security.user.repository;

import com.sambath.security.user.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findUserByEmail(String email); // Find user by email

    // Enable user by email
    @Transactional
    @Modifying
    @Query("UPDATE User u " + "SET u.enabled = TRUE WHERE u.email = ?1")
    void enableUser(String email);

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);
}
