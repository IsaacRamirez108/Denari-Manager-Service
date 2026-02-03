package com.denari.manager.controllers;

import com.denari.manager.models.entity.User.User;
import com.denari.manager.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get user profile with masked SSN - SAFE for regular users
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            String maskedSSN = user.getUserIdentity() != null ?
                    user.getUserIdentity().getSsnMasked() : null;

            Map<String, Object> profile = Map.of(
                    "id", user.getId(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "email", user.getEmail(),
                    "phoneNumber", user.getUserIdentity().getPhoneNumber(),
                    "maskedSSN", maskedSSN != null ? maskedSSN : "Not provided",
                    "hasSSN", maskedSSN != null,
                    "status", user.getStatus().toString()
            );

            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error loading profile");
        }
    }

    /**
     * Get SSN status - SAFE
     */
    @GetMapping("/ssn-status")
    public ResponseEntity<?> getSSNStatus(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);

            boolean hasSSN = user.getUserIdentity() != null &&
                    user.getUserIdentity().getSsnMasked() != null;

            return ResponseEntity.ok(Map.of(
                    "hasSSN", hasSSN,
                    "maskedSSN", hasSSN ? user.getUserIdentity().getSsnMasked() : "Not provided"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error checking SSN status");
        }
    }
}
