package com.projetoresgate.projetoresgate_api.core.identity.user.api.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("AuthenticationResponse - Serialização")
class AuthenticationResponseTest {

    @Test
    @DisplayName("Nunca deve expor o refresh token no JSON")
    void shouldNotSerializeRefreshToken() {
        AuthenticationResponse response = new AuthenticationResponse(
                "access", "refresh-secreto", 3600, "Bearer", "id", "Nome", "a@b.com", Set.of());

        String json = JsonMapper.builder().build().writeValueAsString(response);
        JsonNode node = JsonMapper.builder().build().readTree(json);

        assertFalse(node.has("refreshToken"), json);
        assertFalse(json.contains("refresh-secreto"), json);
        assertEquals("access", node.get("accessToken").asText());
    }
}
