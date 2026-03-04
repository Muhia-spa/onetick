package com.onetick.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onetick.entity.Role;
import com.onetick.entity.User;
import com.onetick.entity.enums.RoleName;
import com.onetick.repository.RoleRepository;
import com.onetick.repository.UserRepository;
import com.onetick.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

@Service
public class OidcAuthService {
    private static final Logger log = LoggerFactory.getLogger(OidcAuthService.class);

    private final OidcProperties properties;
    private final OidcStateStore stateStore;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public OidcAuthService(OidcProperties properties,
                           OidcStateStore stateStore,
                           ObjectMapper objectMapper,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.properties = properties;
        this.stateStore = stateStore;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.restTemplate = new RestTemplate();
    }

    public URI buildLoginRedirect() {
        String state = UUID.randomUUID().toString();
        stateStore.store(state);

        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(properties.getDomain())
                .path("/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.getClientId())
                .queryParam("redirect_uri", properties.getRedirectUri())
                .queryParam("scope", properties.getScopes())
                .queryParam("state", state);

        if (properties.getAudience() != null && !properties.getAudience().isBlank()) {
            builder.queryParam("audience", properties.getAudience());
        }

        return builder.build(true).toUri();
    }

    public String handleCallback(String code, String state) {
        if (!stateStore.consume(state)) {
            throw new IllegalStateException("Invalid OIDC state");
        }

        JsonNode tokenResponse = exchangeCode(code);
        String accessToken = tokenResponse.path("access_token").asText(null);
        if (accessToken == null) {
            throw new IllegalStateException("Missing access token from OIDC provider");
        }

        JsonNode userInfo = fetchUserInfo(accessToken);
        String email = userInfo.path("email").asText(null);
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("OIDC user info missing email");
        }
        String name = resolveName(userInfo);

        Set<Role> roles = resolveRoles(userInfo);
        User user = userRepository.findByEmail(email).orElseGet(User::new);
        user.setEmail(email);
        user.setName(name);
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        }
        user.setActive(true);
        user.setRoles(roles);
        userRepository.save(user);

        List<String> roleNames = roles.stream()
                .map(role -> "ROLE_" + role.getName().name())
                .toList();
        return jwtService.generateToken(user.getEmail(), roleNames);
    }

    private JsonNode exchangeCode(String code) {
        String tokenUrl = "https://" + properties.getDomain() + "/oauth/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", properties.getClientId());
        body.add("client_secret", properties.getClientSecret());
        body.add("code", code);
        body.add("redirect_uri", properties.getRedirectUri());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("OIDC token exchange failed: " + response.getStatusCode());
        }

        try {
            return objectMapper.readTree(response.getBody());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse OIDC token response", ex);
        }
    }

    private JsonNode fetchUserInfo(String accessToken) {
        String userInfoUrl = "https://" + properties.getDomain() + "/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("OIDC user info failed: " + response.getStatusCode());
        }

        try {
            return objectMapper.readTree(response.getBody());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse OIDC user info response", ex);
        }
    }

    private String resolveName(JsonNode userInfo) {
        String name = userInfo.path("name").asText(null);
        if (name != null && !name.isBlank()) {
            return name;
        }
        String given = userInfo.path("given_name").asText("");
        String family = userInfo.path("family_name").asText("");
        String combined = (given + " " + family).trim();
        if (!combined.isBlank()) {
            return combined;
        }
        return userInfo.path("email").asText("Unknown User");
    }

    private Set<Role> resolveRoles(JsonNode userInfo) {
        Set<Role> roles = new HashSet<>();
        Map<String, RoleName> mapping = properties.getGroupRoleMapping();
        if (mapping != null && !mapping.isEmpty()) {
            List<String> groups = extractGroups(userInfo, properties.getGroupClaim());
            for (String group : groups) {
                RoleName roleName = mapping.get(group);
                if (roleName != null) {
                    Role role = roleRepository.findByName(roleName)
                            .orElseThrow(() -> new IllegalStateException("Role not seeded: " + roleName));
                    roles.add(role);
                }
            }
        }

        if (roles.isEmpty()) {
            Role staff = roleRepository.findByName(RoleName.STAFF)
                    .orElseThrow(() -> new IllegalStateException("Role not seeded: STAFF"));
            roles.add(staff);
        }
        return roles;
    }

    private List<String> extractGroups(JsonNode userInfo, String claim) {
        JsonNode node = userInfo.get(claim);
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (node.isArray()) {
            List<String> groups = new ArrayList<>();
            node.forEach(item -> groups.add(item.asText()));
            return groups;
        }
        String single = node.asText();
        if (single == null || single.isBlank()) {
            return List.of();
        }
        return List.of(single);
    }
}
