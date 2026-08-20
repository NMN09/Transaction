package com.wallet.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAndRoleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("PRD Section 16 & Test 6: Missing/Invalid JWT returns HTTP 401 UNAUTHORIZED JSON")
    void protectedEndpoint_WithoutToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/wallet"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.path").value("/api/wallet"));
    }

    @Test
    @DisplayName("PRD Section 16 & Test 17: Normal USER role calling ADMIN endpoint returns HTTP 403 FORBIDDEN JSON")
    @WithMockUser(username = "1", roles = {"USER"})
    void adminEndpoint_WithUserRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.path").value("/api/admin/users"));
    }

    @Test
    @DisplayName("PRD Test 17: ADMIN role can successfully access ADMIN endpoints (200 OK)")
    @WithMockUser(username = "99", roles = {"ADMIN"})
    void adminEndpoint_WithAdminRole_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());
    }
}
