# Assets

| ID | Name | Description | Trust Levels |
|---|---|---|---|
| 1 | Stored Data | Persistent data maintained by the application, including product, category, cart, order, and user-related data. | Customer, Carrier, Manager/Admin |
| 1.1 | User Credentials | Identity and credential data used for authentication and RBAC. Includes account identity and login-related information. | Customer, Carrier, Manager/Admin |
| 1.2 | User Personal Data | Customer and invited-user personal data handled by the system for account and order-related flows. | Customer, Carrier, Manager/Admin |
| 1.3 | Product Data | Product information such as name, description, price, category, and timestamps. | Customer, Carrier, Manager/Admin |
| 1.4 | Category Data | Product category records with unique names and timestamps. | Customer, Carrier, Manager/Admin |
| 1.5 | Cart Data | Shopping cart items, quantities, and computed totals. | Customer |
| 1.6 | Order and Fulfillment Data | Orders, statuses, pickup lifecycle, and order history. | Customer, Carrier, Manager/Admin |
| 1.7 | Reporting Data | Aggregated sales and operational reports used in the backoffice. | Manager/Admin |
| 1.8 | Error and Validation Responses | Error payloads and validation feedback returned by the API. | Customer, Carrier, Manager/Admin |
| 2 | System Services | Services required to operate the platform, including backend, authentication, email, and logging integrations. | Manager/Admin |
| 2.1 | Spring Boot Backend API | Main application service exposing the API endpoints and business logic. | Customer, Carrier, Manager/Admin |
| 2.2 | Authentication Service | External identity provider used for authentication, JWT issuance, and RBAC. | Customer, Carrier, Manager/Admin |
| 2.3 | PostgreSQL Database | Relational database used to store application data. | Manager/Admin |
| 2.4 | Email Service | SMTP service used for registration confirmation, password recovery, and notifications. | Customer, Carrier, Manager/Admin |
| 2.5 | Syslog Server | Centralized logging service used for security-relevant events and auditing. | Manager/Admin |
| 2.6 | API Documentation Service | OpenAPI and Swagger UI endpoints for the backend API. | Customer, Carrier, Manager/Admin |
| 2.7 | Monitoring Endpoints | Actuator health and info endpoints exposed by the backend. | Manager/Admin |
| 3 | Sessions and Tokens | JWTs or session artifacts used to access protected endpoints. | Customer, Carrier, Manager/Admin |
| 4 | Deployment Infrastructure | Runtime and delivery infrastructure used to build, deploy, and host the system. | Manager/Admin |
| 4.1 | Docker Runtime | Containerized backend and supporting services used for deployment. | Manager/Admin |
| 4.2 | Firewall and Network Security | Network controls restricting exposed ports and protecting internal services. | Manager/Admin |
| 4.3 | HTTPS / TLS Certificates | Certificates enforcing encrypted communication on API endpoints. | Manager/Admin |
| 4.4 | CI/CD Pipeline | Automated build, test, and deployment pipeline. | Manager/Admin |
| 4.5 | SAST / SCA Tools | Static analysis and dependency scanning tools integrated into the pipeline. | Manager/Admin |
| 4.6 | Java Runtime Environment | Runtime required to execute the backend application. | Manager/Admin |
| 4.7 | Third-party Libraries | External libraries used by the backend, including Spring Boot, Spring Security, Hibernate, and Bucket4j. | Manager/Admin |
| 4.8 | Bucket4j Rate Limiter | Rate limiting component used on authentication entry points. | Manager/Admin |
| 5 | Configuration Secrets | Environment-based database URL, username, password, JWT secrets, and SMTP credentials. | Manager/Admin |
| 6 | Backups and Recovery Artifacts | Backups and snapshots used for persistent data recovery. | Manager/Admin |
