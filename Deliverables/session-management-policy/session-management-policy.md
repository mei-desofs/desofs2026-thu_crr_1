# Session Management Policy

## 1. Inactivity Timeout
- **Access Token Lifetime**: 1 hour
- **Inactivity Detection**: Client-side (frontend responsible for refresh)
- **Justification**: NIST SP 800-63B recommends 30 minutes for low-risk systems. We use 1 hour as a compromise, as stateless JWTs with trusted backend validation reduce the risk profile. Users are automatically prompted to refresh before expiration.

## 2. Maximum Session Lifetime
- **Refresh Token Lifetime**: 7 days
- **Post-Expiration**: User must re-authenticate from login page
- **Justification**: Aligns with NIST SP 800-63B recommendations for non-high-assurance systems. Token rotation via the refresh endpoint extends session security and limits token exposure window.

## 3. Token Refresh Mechanism
- **Access Token Expired**: Frontend uses Refresh Token endpoint to obtain new access token
- **Refresh Token Expired**: User is redirected to login page
- **Grace Period**: None; re-authentication is required immediately
- **Implementation**: Refresh tokens are invalidated on revocation (logout) via Supabase token revocation endpoint

## 4. Concurrent Sessions Policy
- **Current Implementation**: No limit on concurrent active sessions per user
- **Behavior**: Multiple parallel sessions are allowed simultaneously
  - A user can log in from different devices/browsers at the same time
  - Each login request generates a new JWT pair (access + refresh token)
  - Each token pair is independent

## 5. Federated Identity Management (Supabase OAuth/OIDC)
- **Identity Provider**: Supabase (Auth v2)
- **Integration**: Spring Security OAuth2 with JWT validation
- **Token Generation**: 
  - Access tokens issued by Supabase with ES256 signature
  - Issuer validation: Verified against configured Supabase URL
  - Audience validation: Verified against configured JWT audience
  - JWT decoding: Uses JWKS endpoint for public key verification
- **Session Coordination**:
  - Backend respects access token expiration time from Supabase
  - Refresh tokens are managed via Supabase OAuth2 `/token` endpoint
  - On logout: Backend revokes token on Supabase side
  - Session termination: Token revocation prevents further use of both access and refresh tokens
- **Re-authentication Requirements**:
  - Access token: Must refresh before expiration (1 hour inactivity)
  - Refresh token: Must re-authenticate after 7 days (absolute max lifetime)
  - MFA (if enabled): Enforced by Supabase during initial login

## 6. Session Termination and Revocation
- **Logout Behavior**:
  - Revokes access token on Supabase backend
  - Clears HttpOnly cookies on client
  - Logging: Audit log entry created regardless of revocation success
  - Fail-safe: Logout succeeds locally even if Supabase revocation fails
- **Token Invalidation Strategy**: 
  - Uses Supabase token revocation API
  - Revoked tokens are blacklisted on the Supabase side
  - Attempts to use revoked tokens are rejected by `JWTAuthFilter`

## 7. Cookie Security Settings
- **Transport**: HttpOnly + Secure flags set
  - Protects against XSS token theft
  - Only sent over HTTPS in production
- **SameSite**: Strict mode to prevent CSRF attacks
- **Path**: Root path "/" to allow access across all endpoints
- **Expiration**: Matches token lifetime (1 hour access, 7 days refresh)