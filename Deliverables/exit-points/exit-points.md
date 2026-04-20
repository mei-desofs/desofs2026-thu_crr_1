# Exit Points

| ID | Name | Description | Data Sent | Potential Vulnerabilities |
|---|---|---|---|---|
| 1 | TLS-Encrypted Responses | Secure transport for all API traffic. | All HTTP responses wrapped in TLS encryption | TLS downgrade, weak ciphers, missing HSTS header, certificate pinning absence |
| 2 | API Documentation Endpoints | Public API specification and interactive documentation. | OpenAPI schema `/api/v3/api-docs`, Swagger UI `/api/swagger-ui.html` | Unauthenticated API reconnaissance, endpoint list leakage |
| 3 | Actuator Endpoints | Operational health and information exposure. | Health `/api/actuator/health`, Info `/api/actuator/info` | System fingerprinting, version disclosure, operational metadata leakage |
| 4 | Register Endpoint Response | Returns either success or validation/error output from registration. | User Data, Auth Data, Validation/Error Data | User enumeration, validation feedback leakage, weak password policy |
| 5 | Login Endpoint Response | Returns either authentication output or authentication failure feedback. | Auth Data, User Data, Validation/Error Data | Brute force, credential stuffing, token leakage, user enumeration |
| 6 | Logout Endpoint Response | Returns logout confirmation or related error feedback. | Session Data, Validation/Error Data | Token not revoked server-side, missing audit trail |
| 7 | Refresh Token Endpoint Response | Returns refreshed session tokens or token-related errors. | Auth Data, Session Data, Validation/Error Data | Refresh token theft, weak rotation, unlimited refresh cycles |
| 8 | Invite Endpoint Response | Returns invite creation output or invite validation/error feedback. | User Data, Invitation Data, Validation/Error Data | Invitation forgery, privilege escalation, token misuse |
| 9 | Confirm Invite Endpoint Response | Returns invite confirmation output or confirmation errors. | User Data, Auth Data, Validation/Error Data | Token reuse, role manipulation, weak token validation |
| 10 | Reset Password Endpoint Response | Returns reset initiation/completion output or validation/errors. | User Data, Recovery Data, Validation/Error Data | Reset abuse, token interception, weak reset policy |
| 11 | Product Catalog Read Responses | Returns product listing/search data and related errors. | Product Data, Pagination Data, Validation/Error Data | Data overexposure, enumeration, query abuse |
| 12 | Create Product Endpoint Response | Returns create result or create validation/error feedback. | Product Data, Validation/Error Data | Broken access control, mass assignment, business logic abuse |
| 13 | Update Product Endpoint Response | Returns update result or update validation/error feedback. | Product Data, Validation/Error Data | IDOR, broken access control, weak change auditing |
| 14 | Cart Read Endpoint Response | Returns current cart state or cart retrieval errors. | Cart Data, User Data, Validation/Error Data | Broken object-level authorization, data leakage |
| 15 | Cart Mutation Endpoint Responses | Returns cart changes for add/update/remove actions or mutation errors. | Cart Data, Product Data, Validation/Error Data | Quantity/price tampering, race conditions, cross-user cart manipulation |
| 16 | Place Order Endpoint Response | Returns order creation result or validation/conflict errors. | Order Data, User Data, Validation/Error Data | TOCTOU race condition, price tampering, authorization flaws |
| 17 | Order Read Endpoint Responses | Returns order history/details or read errors. | Order Data, User Data, Validation/Error Data | IDOR, PII leakage, order enumeration |
| 18 | Email Notification Output | Sends transactional notifications to users. | Notification Data, User Data, Order Data | Email spoofing, sensitive data leakage, delayed/duplicate notifications |
| 19 | Carrier Queue Endpoint Response | Returns assigned orders or queue access errors. | Order Data, User Data, Validation/Error Data | PII exposure, broken authorization |
| 20 | Carrier Pickup Update Response | Returns state transition result or transition errors. | Order Data, Status Data, Validation/Error Data | State machine bypass, integrity issues, status disclosure |
| 21 | Backup Endpoint Response | Returns backup initiation/output or backup errors. | Backup Data, Operational Metadata, Validation/Error Data | Unencrypted backups, access control failures, sensitive data exposure |
| 22 | Backup Completion Notification | Returns backup-ready notifications through async channels. | Backup Data, Notification Data, Operational Metadata | URL tampering, weak signature checks, link expiration bypass |