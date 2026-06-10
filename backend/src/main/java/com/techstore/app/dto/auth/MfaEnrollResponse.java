package com.techstore.app.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MfaEnrollResponse(
        String id,           // factorId — needed for subsequent challenge/verify calls
        String type,         // "totp"
        String status,
        @JsonProperty("totp") TotpData totp
) {
    public record TotpData(
            @JsonProperty("qr_code") String qr_code,  // data:image/svg+xml URI for the QR code
            String secret,   // base32 secret (show to user as fallback)
            String uri       // otpauth:// URI
    ) {}
}