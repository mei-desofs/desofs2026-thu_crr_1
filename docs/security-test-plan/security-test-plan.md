### Security Testing Plan

#### Testing Methodology

| Test Type | Purpose |
|------------|------|
| Unit Tests | Validate security rules and logic in isolated components |
| Integration Tests | Validate interactions between components and security controls |
| Functional Automatic Tests | Validate endpoint behavior in CI/CD (using tools like Postman/Newman) |
| Functional Manual Tests | Validate realistic attack/use flows and edge cases |
| SAST | Detect vulnerabilities in code/dependencies |
| DAST | Detect vulnerabilities while API is running |

#### Abuse Cases (Reference for Test Planning)

Below are two examples of how abuse cases map to security tests:

| Abuse Case | Security Test Type | Expected Result |
|------------|--------------------|--------------|
| Forge webhook request to create user with other role| Functional test: call webhook endpoint without valid secret or with manipulated payload | Request rejected with 403; no user created |
| Email enumeration in invite | Functional test: submit existing and non-existing emails to invite and reset endpoints | Both return equivalent responses with no account disclosure |

#### Threat Modelling Review Process

Threat model must be reviewed when:

1. A new endpoint/feature is added.
2. Architecture or trust boundaries change.
3. Before each release.

Simple workflow:

1. Update DFD/abuse case.
2. Re-check STRIDE threats.
3. Confirm mitigations and residual risk.
4. Add or update related security tests.

#### ASVS Assessment

The system will be assessed against the OWASP ASVS v5.0.0 Level 2, which is appropriate for an e-commerce API handling personal data such as emails, addresses, and tax identification numbers, with role-based authentication.

This assessment will be document in a xlsx file that contains a checklist of all ASVS requirements. For each requirement, its is necessary indicates the status of implementation (not started, in progress, complient, not applicable), the observations and the reference / link to the related code, test, or documentation that demonstrates compliance. This document will be updated as the system evolves and will be used as a reference for security reviews and audits.

The document is here: [ASVS Assessment](../asvs-assessment/ASVS_5.0_Tracker.xlsx)

#### Traceability (Security Requirements to Tests)

Use the table below as a living checklist.

| Security Requirement | Test Type | Example Test |
|----------------------|-----------|--------------|
| SR1 MFA | functional automatic + manual | Login requires second factor |
| SR2 Lockout | functional automatic | 5 failed logins trigger lockout |
| SR3 Password policy | unit + functional automatic | Password rejected if under 12 chars or missing required character types |
| SR4 Registration email | manual | Confirmation email sent after successful registration |
| SR5 RBAC | unit + integration + functional automatic | Customer blocked from manager routes |
| SR6 Session Expiry | functional automatic | Session invalidated after inactivity period |
| SR7 Rate limiting | functional automatic + integration + manual | Burst requests return throttling response |
| SR8 Encryption at rest and in transit | review + functional automatic | Sensitive data encrypted in database and transmitted over HTTPS |
| SR9 Password hashing | review + unit + integration | Passwords hashed with bcrypt and verified correctly |
| SR10 GDPR compliance | review + manual | Personal data handling reviewed for GDPR compliance |
| SR11 Input validation | unit + DAST | Malicious payload rejected safely |
| SR12 Data format validation | unit + functional automatic | Malformed input rejected with appropriate error response |
| SR13 Secure logs | functional automatic + review | Sensitive action logged without secret exposure |
| SR14 Log backup | review + manual | Three backup copies verified across local and cloud storage |

#### Release Security Gate

Before each release, the following security gate must be passed:

1. Critical vulnerabilities: 0 open.
2. Unit tests: 100% passing.
3. Integration tests: 100% passing.
4. Functional tests: 100% passing.
5. High-priority abuse-case tests: passing.
6. Threat model review: completed.
7. ASVS assessment; updated.
8. Traceability table: updated.

