package com.techstore.app.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterResponse(
        String email,
        @JsonProperty("user_id") String userId,
        String message
) {
}
