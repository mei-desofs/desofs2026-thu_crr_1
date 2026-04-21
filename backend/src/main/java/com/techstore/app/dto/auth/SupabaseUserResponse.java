package com.techstore.app.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SupabaseUserResponse(String id, String email,
                                   @JsonProperty("app_metadata") AppMetadata appMetadata) {
    public String getRole() {
        return appMetadata != null ? appMetadata.role() : "USER";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AppMetadata(String role) {}
}
