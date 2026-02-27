package com.onetick.controller;

import com.onetick.dto.request.AuthRequest;
import com.onetick.dto.response.AuthResponse;
import com.onetick.security.JwtService;
import com.onetick.service.impl.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserDetailsServiceImpl userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        var userDetails = userDetailsService.loadUserByUsername(authentication.getName());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        String token = jwtService.generateToken(userDetails.getUsername(), roles);
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
