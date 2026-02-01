package com.tanmay.secure_e_commerce.service;

import com.tanmay.secure_e_commerce.dto.AuthRequest;
import com.tanmay.secure_e_commerce.dto.AuthResponse;
import com.tanmay.secure_e_commerce.entity.User;
import com.tanmay.secure_e_commerce.enums.Role;
import com.tanmay.secure_e_commerce.repository.UserRepository;
import com.tanmay.secure_e_commerce.security.CustomUserDetailsService;
import com.tanmay.secure_e_commerce.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    //Login
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final User user = userDetailsService.getUserByUsername(request.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails, user.getRole().name());

        return new AuthResponse(jwt, user.getUsername(), user.getRole().name());
    }

    //register
    public User register(String username, String password, String email, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(role);

        return userRepository.save(user);
    }
}
