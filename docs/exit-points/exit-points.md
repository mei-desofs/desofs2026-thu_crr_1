# Exit Points

This document identifies system exit points (outbound responses/interfaces) based on:
- current backend implementation in `backend/src/main`
- functional requirements listed in `docs/README.md`

Because the project is still incomplete, each entry includes a status:
- Implemented: exists in code now
- Planned: required/documented but not implemented yet

## Exit Points Table

| ID | Name | Description | Data Sent | Potential Vulnerabilities | Status |
|---|---|---|---|---|---|
| EP-1 | HTTPS API Responses | All backend responses should be returned over encrypted HTTPS channels. | JSON responses and error payloads | TLS misconfiguration, weak ciphers, missing HSTS | Planned |
| EP-1.1 | Create Product Response | Response after creating a product (`POST /api/products`). | `id`, `name`, `description`, `price`, `categoryName` | Broken access control (currently open), mass assignment risk, input validation bypass | Implemented |
| EP-1.2 | Search Products Response | Paginated product search result (`GET /api/products/search?productName=`). | Page metadata + list of product summaries (`id`, `name`, `description`, `price`, `categoryName`) | Excessive data exposure, enumeration via broad search, query abuse without rate limit | Implemented |
| EP-1.3 | Error Response Contract | Standardized error output from global exception handler. | `status`, `message`, `error`, `timestamp`, `path`, optional `fieldErrors`/`errors` | Internal path disclosure, verbose error details, inconsistent error sanitization | Implemented |
| EP-1.4 | API Documentation Endpoints | OpenAPI docs and Swagger UI endpoints. | API schema and endpoint metadata (`/api/v3/api-docs`, `/api/swagger-ui.html`) | Endpoint reconnaissance, unauthenticated API discovery | Implemented |
| EP-1.5 | Health and Info Endpoints | Actuator management endpoints for service status. | Application health/info (`/api/actuator/health`, `/api/actuator/info`) | Environment intelligence leakage, monitoring endpoint abuse | Implemented |
| EP-2.1 | Authentication Responses | Login/logout/register/password recovery flows required in FR1-FR4. | Tokens/session data, auth status, user identity metadata | Token leakage, credential stuffing feedback, user enumeration | Planned |
| EP-2.2 | Profile Management Responses | Customer profile read/update responses from FR3. | Personal data updates and confirmation status | PII exposure, broken object-level authorization | Planned |
| EP-2.3 | Product Catalog Detail/List Responses | Product listing/detail/filter responses from FR5-FR8/FR21-FR23. | Product catalog and stock data | Excessive data disclosure, insecure direct object reference | Partially implemented |
| EP-2.4 | Cart Operation Responses | Add/remove/update cart operations and totals from FR9-FR12. | Cart contents, quantity updates, total amounts | Price tampering, race conditions, parameter manipulation | Planned |
| EP-2.5 | Order Workflow Responses | Place order, stock validation, order history/status, confirmations from FR13-FR17. | Order IDs, status, stock validation result, notifications | Order enumeration, inconsistent stock checks, privacy leaks | Planned |
| EP-2.6 | Carrier Operation Responses | Pickup list/details/status updates for carrier role from FR18-FR20. | Order pickup queue and pickup confirmations | Unauthorized status updates, workflow tampering | Planned |
| EP-2.7 | Manager Backoffice Responses | Product/category/order/report management from FR21-FR27. | Admin operation results, order views, sales report data | Privilege escalation, insecure role checks, report data leakage | Planned |

