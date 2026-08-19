package com.wallet.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAndRoleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("PRD Test 6: Protected endpoint without JWT is rejected (403 Forbidden)")
    void protectedEndpoint_WithoutToken_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/wallet"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PRD Test 17: Normal USER role cannot access ADMIN endpoints (403 Forbidden)")
    @WithMockUser(username = "1", roles = {"USER"})
    void adminEndpoint_WithUserRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PRD Test 17: ADMIN role can successfully access ADMIN endpoints (200 OK)")
    @WithMockUser(username = "99", roles = {"ADMIN"})
    void adminEndpoint_WithAdminRole_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());
    }
}
