package com.techstore.app.dto.auth;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MfaStatusResponse(
        @JsonProperty("factors") List<Factor> factors
) {
    public List<Factor> factors() {
        return factors != null ? factors : List.of();
    }

    public boolean hasVerifiedFactor() {
        return factors().stream()
                .anyMatch(f -> "totp".equals(f.type()) && "verified".equals(f.status()));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Factor(
            String id,
            @JsonProperty("factor_type") String type,
            String status,
            @JsonProperty("friendly_name") String friendlyName
    ) {}
}
