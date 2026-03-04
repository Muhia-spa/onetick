package com.onetick.oidc;

import com.onetick.entity.enums.RoleName;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "app.oidc.auth0")
public class OidcProperties {
    private boolean enabled = false;
    private String domain;
    private String clientId;
    private String clientSecret;
    private String audience;
    private String redirectUri;
    private String scopes = "openid profile email";
    private String groupClaim = "groups";
    private long stateTtlMs = 300000;
    private Map<String, RoleName> groupRoleMapping = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public String getGroupClaim() {
        return groupClaim;
    }

    public void setGroupClaim(String groupClaim) {
        this.groupClaim = groupClaim;
    }

    public long getStateTtlMs() {
        return stateTtlMs;
    }

    public void setStateTtlMs(long stateTtlMs) {
        this.stateTtlMs = stateTtlMs;
    }

    public Map<String, RoleName> getGroupRoleMapping() {
        return groupRoleMapping;
    }

    public void setGroupRoleMapping(Map<String, RoleName> groupRoleMapping) {
        this.groupRoleMapping = groupRoleMapping;
    }
}
