# Countermeasures and Mitigations

These are the terms used in the context of cybersecurity to describe actions taken to reduce or eliminate the vulnerabilities and risks associated with cyber threats. Once the threats and the corresponding vulnerabilities have been identified, it is possible to derive a threat profile with the following criteria:

- **Non mitigated threats**: Threats which have no countermeasures and represent vulnerabilities that can be fully exploited and cause an impact.
- **Partially mitigated threats**: Threats partially mitigated by one or more countermeasures and can only partially be exploited to cause a limited impact.
- **Full mitigated threats**: These threats have appropriate countermeasures in place and do not expose vulnerabilities.

In the next table we can see the implementation of the countermeasures in the premise of this project.

| STRIDE                  | Countermeasures |
|-------------------------|----------------|
| **Spoofing**            | - Strong password policy <br/> - Multi-Factor Authentication (MFA) <br/> - Token-based authentication <br/> - Rate limiting and account lockout mechanisms <br/> - Secure session management (timeouts, rotation) <br/> - Do not store secrets in code |
| **Tampering**           | - Input sanitization and validation <br/> - Code audits <br/> - Use HTTPS/TLS for data in transit <br/> - Integrity checks (HMAC, digital signatures) <br/> - Secure hashing with salt <br/> - Proper authorization |
| **Repudiation**         | - Timestamps <br/> - Audit trails and logging <br/> - Centralized logging and monitoring <br/> - Log integrity protection (append-only, signed logs) |
| **Information Disclosure** | - Encryption (data at rest and in transit) <br/> - Proper authorization and access control <br/> - Do not store secrets in code <br/> - Sanitize error messages (avoid verbose errors) <br/> - Prevent user/email enumeration <br/> - Secure logging (no sensitive data exposure) <br/> - Data minimization and masking |
| **Denial of Service**   | - Rate limiting and throttling <br/> - Filtering (WAF, IP blocking) <br/> - CAPTCHA / bot detection <br/> - Load balancing and autoscaling <br/> - Quality of Service (QoS) mechanisms |
| **Elevation of Privilege** | - Least privilege principle <br/> - Role-Based Access Control (RBAC) <br/> - Proper authorization checks <br/> - Privilege separation <br/> - Secure coding practices |