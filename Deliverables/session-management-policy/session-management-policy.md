# Session Management Policy

## Inactivity Timeout
- Access Token Lifetime: 1 hour
- Inactivity detection: Client-side
- Justification: NIST SP 800-63B recommends 30 min (low-risk), 
  we use 1h as compromise (stateless JWT reduces risk)

## Maximum Session Lifetime
- Refresh Token Lifetime: 7 days
- After 7 days: User must re-authenticate
- Justification: Aligns with NIST recommendations for non-high-assurance
  systems. Token rotation via refresh extends session securely.

## Token Refresh Mechanism
- Access Token expired: Frontend uses Refresh Token
- Refresh Token expired: User redirected to login
- No grace period