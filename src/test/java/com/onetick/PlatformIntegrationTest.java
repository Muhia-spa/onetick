package com.onetick;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlatformIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginWithSeededAdminShouldSucceed() throws Exception {
        String body = """
                {
                  "email":"admin@onetick.local",
                  "password":"admin12345"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void departmentsListWithoutTokenShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isForbidden());
    }

    @Test
    void workspaceAndProjectTablesShouldExistAfterMigration() {
        Integer workspaceTable = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'workspaces'",
                Integer.class
        );
        Integer projectTable = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'projects'",
                Integer.class
        );
        Integer auditLogTable = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'audit_logs'",
                Integer.class
        );

        assertThat(workspaceTable).isNotNull();
        assertThat(projectTable).isNotNull();
        assertThat(auditLogTable).isNotNull();
        assertThat(workspaceTable).isGreaterThan(0);
        assertThat(projectTable).isGreaterThan(0);
        assertThat(auditLogTable).isGreaterThan(0);
    }

    @Test
    void creatingWorkspaceShouldWriteAuditLog() throws Exception {
        String token = loginAndGetToken("admin@onetick.local", "admin12345");

        mockMvc.perform(post("/api/v1/workspaces")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Audit Workspace",
                                  "code":"AUDIT_WS"
                                }
                                """))
                .andExpect(status().isCreated());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_logs WHERE action = 'WORKSPACE_CREATE'",
                Integer.class
        );
        assertThat(count).isNotNull();
        assertThat(count).isGreaterThan(0);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"%s",
                                  "password":"%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }
}
