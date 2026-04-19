# DESOFS - PART 1

## Functional Requirements

| ID | Requirement |
|----|-------------|
| **FR1** | The system shall allow anonymous users to register as a customer. |
| **FR2** | The system shall allow users to log in and log out. |
| **FR3** | The system shall provide password recovery functionality. |
| **FR4** | The system shall allow authenticated users to refresh their JWT token before expiration. |
| **FR5** | The manager user shall be able to invite new users (managers or carriers) to the system. |
| **FR6** | The system shall allow invited users to complete their registration process. |
| **FR7** | The system shall display a list of available products. |
| **FR8** | The system shall allow users to search for products by name. |
| **FR9** | The system shall display product details (price, description, stock). |
| **FR10** | The customer shall be able to add products to the cart. |
| **FR11** | The customer shall be able to remove products from the cart. |
| **FR12** | The customer shall be able to update product quantities from the Cart. |
| **FR13** | The system shall automatically calculate the cart total. |
| **FR14** | The customer shall be able to place an order from the cart. |
| **FR15** | The system shall validate product stock before confirming the order. |
| **FR16** | The system shall store the user's order history. |
| **FR17** | The customer shall be able to view the order status. |
| **FR18** | The system shall send an order confirmation email to the user. |
| **FR19** | The carrier user shall view a list of orders ready for pickup. |
| **FR20** | The carrier user shall display relevant order information for pickup. |
| **FR21** | The carrier user shall mark an order as picked up. |
| **FR22** | The manager user shall be able to add new products. |
| **FR23** | The manager user shall be able to edit existing product information. |
| **FR24** | The manager user shall be able to manage product categories. |
| **FR25** | The manager user shall be able to update product stock levels manually. |
| **FR26** | The manager user shall be able to view and filter all customer orders in the backoffice. |
| **FR27** | The manager user shall be able to create a backup of products, categories, and orders data. |

## Non-Functional Requirements
| ID        | Requirement                                                                                                                                               |
|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| **NFR1**  | The API must be available only through HTTPS (TLS 1.2 or higher) in non-local environments.                                                               |
| **NFR2**  | User passwords must be hashed with a strong adaptive algorithm (e.g., BCrypt) before persistence.                                                         |
| **NFR3**  | The system must enforce role-based access control (RBAC) with deny-by-default authorization.                                                              |
| **NFR4**  | The API must mitigate common web threats (SQL Injection, XSS, CSRF where applicable) through input validation, parameterized queries, and secure headers. |
| **NFR5**  | Security-relevant actions (authentication, authorization failures, and critical data changes) must be logged with timestamp and user context.             |
| **NFR6**  | Two-factor authentication is mandatory to all users.                                                                                                      |
| **NFR7**  | The system must handle at least 100 concurrent requests with response time below 500 ms.                                                                  |
| **NFR8**  | The codebase must follow clean architecture principles.                                                                                                   |
| **NFR9**  | CI/CD pipelines must run automatically on pull requests and include build, tests, and security checks before merge.                                       |
| **NFR10** | The application must be deployable as containers using non-root execution and minimal runtime image principles (preferably with Docker Hardened images).  |
| **NFR11** | The API must follow REST conventions, returning correct HTTP status codes and consistent structured error responses.                                      |
| **NFR12** | The API must provide and maintain OpenAPI documentation for all public endpoints, inputs, outputs, and error cases.                                       |
| **NFR13** | Dependency vulnerability scanning (SCA) must run in CI, with no Critical vulnerabilities allowed in release builds.                                       |
| **NFR14** | Automated tests must ensure minimum 80% line coverage in service/domain layers and include security-related test cases.                                   |
| **NFR15** | Secrets (keys, tokens, passwords) must not be stored in source code; secret scanning must be enabled in the repository.                                   |