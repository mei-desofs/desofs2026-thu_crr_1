# Assets

This document identifies key business and technical assets for threat modeling, combining:
- implemented backend assets (entities, database, runtime endpoints)
- planned assets from requirements and architecture docs

Status legend:
- Implemented: present in code/infrastructure now
- Planned: required/documented but not implemented yet

## Assets Table

| ID | Name | Description | Trust Levels | Security Concerns | Status |
|---|---|---|---|---|---|
| A-1 | Stored Data | Persistent business data managed by the platform. | App service, database administrators | Unauthorized read/write, data corruption, weak backup policy | Partially implemented |
| A-1.1 | Product Data | Product records: name, description, price (`Money`), category, timestamps. | Backend service, manager/backoffice users (future) | Catalog tampering, integrity and price manipulation | Implemented |
| A-1.2 | Category Data | Product category records with unique names and timestamps. | Backend service, manager/backoffice users (future) | Taxonomy tampering, duplicate/inconsistent categories | Implemented |
| A-1.3 | Error and Validation Metadata | Error payload contract and validation feedback returned by API. | API clients, backend service | Information disclosure (`path`, validation internals), abuse for endpoint discovery | Implemented |
| A-1.4 | User Identity and Credential Data | Accounts, passwords, profile, and role data needed by FR1-FR4. | End users, identity provider, admins | Credential theft, PII leakage, privilege abuse | Planned |
| A-1.5 | Cart Data | Cart items, quantities, and computed totals from FR9-FR12. | Authenticated customer, backend service | Price tampering, stale totals, unauthorized cart access | Planned |
| A-1.6 | Order and Fulfillment Data | Orders, statuses, pickup lifecycle, and history from FR13-FR20/FR26. | Customer, carrier, manager, backend service | Order enumeration, status manipulation, privacy violations | Planned |
| A-1.7 | Reporting Data | Aggregated sales and operational reports from FR27. | Manager role | Business intelligence leakage, report tampering | Planned |
| A-2 | System Services | Runtime services required to operate the platform. | Infrastructure admins, operators | Service compromise, insecure configuration | Partially implemented |
| A-2.1 | Spring Boot Backend API | Main service exposing product endpoints and business logic. | Internal service trust boundary, external API clients | Broken access control, input abuse, exception handling leakage | Implemented |
| A-2.2 | PostgreSQL Database | Persistent data store configured via environment variables and Docker. | Backend service, DB admins | SQL injection impact, exposed DB port, weak credentials | Implemented |
| A-2.3 | Authentication Service | External or internal identity/authN/authZ service anticipated by architecture docs. | All authenticated roles | Token forgery, auth bypass, misconfigured RBAC | Planned |
| A-2.4 | Email/Notification Service | Outbound email for confirmations and account flows (FR4, FR17). | Backend service, customer | Notification spoofing, email content leakage | Planned |
| A-2.5 | API Documentation Service | OpenAPI + Swagger UI endpoints for backend APIs. | Developers, integrators | API surface reconnaissance | Implemented |
| A-2.6 | Monitoring Endpoints | Actuator `health`/`info` endpoints exposed over web. | Operators, monitoring systems | Operational metadata disclosure | Implemented |
| A-3 | Sessions and Tokens | JWT/session artifacts needed for protected endpoints and role checks. | Customers, carriers, managers, admins | Token theft, replay, improper expiration/revocation | Planned |
| A-4 | Deployment Infrastructure | Runtime and delivery infrastructure. | DevOps/infra administrators | Supply-chain risks, secrets exposure, misconfiguration | Partially implemented |
| A-4.1 | Dockerized Database Runtime | Containerized PostgreSQL deployment via `docker-compose.yml`. | Infrastructure admins | Container breakout, exposed host networking, secret leakage in env vars | Implemented |
| A-4.2 | CI/CD Pipeline | Automated build/test/deploy process (not yet visible in repo). | DevOps/admins | Pipeline poisoning, unsigned artifacts, insecure secrets handling | Planned |
| A-5 | Configuration Secrets | Environment-based DB URL, username, password, database name. | Backend runtime, infra admins | Secret leakage in logs/repos, weak credential rotation | Implemented |
| A-6 | Backups and Recovery Artifacts | Backups/snapshots for persistent data recovery. | Infrastructure admins | Unencrypted backups, incomplete restore testing, unauthorized access | Planned |
