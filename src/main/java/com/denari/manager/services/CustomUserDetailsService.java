package com.denari.manager.services;

import com.denari.manager.models.entity.User.User;
import com.denari.manager.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // Since we're using phone/OTP auth, we'll use a dummy password
        // The actual authentication happens in AuthService via OTP verification
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password("{noop}") // No password needed - using {noop} encoder
                .authorities("ROLE_USER") // Basic user role
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(user.getStatus() != User.UserStatus.ACTIVE && user.getStatus() != User.UserStatus.PENDING)
                .build();
    }
}
