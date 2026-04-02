# Assets

| ID | Name | Description | Trust Levels |
|---|---|---|---|
| 1 | Stored Data | Persistent business data managed by the platform, including product, category, cart, order, and user-related information. | App service, database administrators |
| 2 | Product Data | Product records such as name, description, price, category, and timestamps. | Backend service, manager/backoffice users |
| 3 | Category Data | Product category records with unique names and timestamps. | Backend service, manager/backoffice users |
| 4 | Error and Validation Metadata | Error payloads and validation feedback returned by the API. | API clients, backend service |
| 5 | User Identity and Credential Data | Accounts, passwords, profile, and role data required by authentication and authorization flows. | End users, identity provider, admins |
| 6 | Cart Data | Cart items, quantities, and computed totals for customer shopping sessions. | Authenticated customer, backend service |
| 7 | Order and Fulfillment Data | Orders, statuses, pickup lifecycle, and history for order processing and delivery. | Customer, carrier, manager, backend service |
| 8 | Reporting Data | Aggregated sales and operational reports used in the backoffice. | Manager role |
| 9 | System Services | Runtime services required to operate the platform. | Infrastructure admins, operators |
| 10 | Spring Boot Backend API | Main application service exposing product endpoints and business logic. | Internal service trust boundary, external API clients |
| 11 | PostgreSQL Database | Persistent data store configured through environment variables and Docker. | Backend service, DB admins |
| 12 | Authentication Service | External or internal authentication and authorization service anticipated by the architecture. | All authenticated roles |
| 13 | Email/Notification Service | Outbound email service for confirmations and account flows. | Backend service, customer |
| 14 | API Documentation Service | OpenAPI and Swagger UI endpoints for backend APIs. | Developers, integrators |
| 15 | Monitoring Endpoints | Actuator health and info endpoints exposed over web. | Operators, monitoring systems |
| 16 | Sessions and Tokens | JWT or session artifacts used to control protected endpoint access. | Customers, carriers, managers, admins |
| 17 | Deployment Infrastructure | Runtime and delivery infrastructure supporting the system. | DevOps/infra administrators |
| 18 | Dockerized Database Runtime | Containerized PostgreSQL deployment via docker-compose. | Infrastructure admins |
| 19 | CI/CD Pipeline | Automated build, test, and deployment process. | DevOps/admins |
| 20 | Configuration Secrets | Environment-based database URL, username, password, and database name. | Backend runtime, infra admins |
| 21 | Backups and Recovery Artifacts | Backups and snapshots used for persistent data recovery. | Infrastructure admins |
