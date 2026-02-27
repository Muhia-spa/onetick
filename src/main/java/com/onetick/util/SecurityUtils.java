package com.onetick.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;
import java.util.stream.Collectors;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }

    public static Set<String> currentRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return Set.of();
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    public static boolean hasRole(String role) {
        return currentRoles().contains("ROLE_" + role);
    }
}
