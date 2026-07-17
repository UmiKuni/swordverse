package com.swordverse.server.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerRefreshReuseDetectionAndSessionRevocation() throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "rotation-user",
                                  "password": "StrongPassword123!",
                                  "displayName": "Rotation User"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.user.username").value("rotation-user"))
                .andReturn();

        JsonNode registerBody = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        String firstAccessToken = registerBody.get("accessToken").asString();
        MockCookie firstRefreshCookie = refreshCookie(registerResult);

        assertThat(firstRefreshCookie.isHttpOnly()).isTrue();
        assertThat(firstRefreshCookie.getPath()).isEqualTo("/api/auth");
        assertThat(firstRefreshCookie.getSameSite()).isEqualTo("Strict");

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + firstAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("rotation-user"));

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(firstRefreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        String secondAccessToken = objectMapper
                .readTree(refreshResult.getResponse().getContentAsString())
                .get("accessToken")
                .asString();
        MockCookie secondRefreshCookie = refreshCookie(refreshResult);
        assertThat(secondRefreshCookie.getValue()).isNotEqualTo(firstRefreshCookie.getValue());

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(firstRefreshCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("TOKEN_REUSE_DETECTED"));

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + secondAccessToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(secondRefreshCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("TOKEN_REUSE_DETECTED"));
    }

    @Test
    void logoutRevokesSessionAndClearsRefreshCookie() throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "logout-user",
                                  "password": "StrongPassword123!",
                                  "displayName": null
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String accessToken = objectMapper
                .readTree(registerResult.getResponse().getContentAsString())
                .get("accessToken")
                .asString();

        MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andReturn();

        MockCookie clearedCookie = refreshCookie(logoutResult);
        assertThat(clearedCookie.getMaxAge()).isZero();

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginUsesGenericInvalidCredentialsError() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "missing-user",
                                  "password": "WrongPassword123!"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    private static MockCookie refreshCookie(MvcResult result) {
        String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).isNotBlank();
        return MockCookie.parse(setCookie);
    }
}
