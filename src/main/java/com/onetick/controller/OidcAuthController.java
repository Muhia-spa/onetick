package com.onetick.controller;

import com.onetick.dto.response.AuthResponse;
import com.onetick.oidc.OidcAuthService;
import com.onetick.oidc.OidcProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth/oidc")
public class OidcAuthController {
    private final OidcAuthService oidcAuthService;
    private final OidcProperties properties;

    public OidcAuthController(OidcAuthService oidcAuthService, OidcProperties properties) {
        this.oidcAuthService = oidcAuthService;
        this.properties = properties;
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login() {
        if (!properties.isEnabled()) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
        URI redirect = oidcAuthService.buildLoginRedirect();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(redirect);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/callback")
    public ResponseEntity<AuthResponse> callback(@RequestParam String code, @RequestParam String state) {
        if (!properties.isEnabled()) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
        String token = oidcAuthService.handleCallback(code, state);
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
