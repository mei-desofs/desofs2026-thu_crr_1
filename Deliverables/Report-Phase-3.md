# Project - Phase 3

| Name             | Student Number |
| ---------------- | -------------: |
| Diogo Martins    |        1221223 |
| Francisco Osorio |        1220846 |
| Joao Pinto       |        1220663 |
| Francisco Reis   |        1201373 |
| Marco Marques    |        1250685 |

# Introduction

In this report, its presentend the phase 3 of the DESOFS project, which consists in the development of the software, following the best practices and security measures defined in the previous phases. The main goal of this phase is to implement the software according to the requirements and design defined in the previous phases, while ensuring that the code is maintainable, scalable and secure.

# Project Overview

**TechStore** consists of the development of a secure e-commerce platform designed to support online product sales, order management, and delivery operations. The system provides a set of RESTful services and a web-based user interface, enabling customers, managers, and carriers to interact with the platform according to their assigned roles.

Additionally, the system provides administrative functionalities that allow managers to maintain product catalogs, manage categories, monitor orders, and oversee platform operations.

## Key Features

- User Management: Handling user registration, authentication, account verification, password recovery, and role-based access control for customers, managers, and carriers.
- Product Management: Allowing managers to create, update, and remove products and categories while providing customers with access to product information and availability.
- Shopping Cart Management: Enabling customers to manage cart contents before completing purchases.
- Order Management: Supporting order creation, tracking, and status management throughout the purchase lifecycle.
- Delivery Operations: Allowing carriers to view assigned deliveries, manage order pickups, and update delivery-related information.
- Administrative Functions: Providing managers with access to operational functionality, system monitoring capabilities, and business-related management features.
- Security Controls: Implementing secure authentication, authorization, HTTPS communication, restricted CORS policies, Content Security Policy (CSP), and additional browser security protections aligned with industry best practices.

The system architecture consists of a Next.js web frontend, a Spring Boot REST API backend, and a relational database for persistent data storage. An NGINX reverse proxy is used to route traffic and enforce secure communication. The application is designed to be scalable, maintainable, and secure while providing a reliable online shopping experience for all user roles.

# Full Workflow

<img src="./images/phase-3/feature-workflow.png" alt="Feature Branch Workflow" width="600">

<img src="./images/phase-3/dev-workflow.png" alt="Dev Branch Workflow" width="600">

<img src="./images/phase-3/main-workflow.png" alt="Main Branch Workflow" width="1400">


# Requirements Implemented

## Functional Requirements

| ID  | Requirement | Status |
|-----|-------------|--------|
| FR1 | Allow anonymous users to register as a customer | <span style="color: green;">Done</span> |
| FR2 | Allow users to log in and log out | <span style="color: green;">Done</span> |
| FR3 | Provide password recovery functionality | <span style="color: green;">Done</span> |
| FR4 | Allow authenticated users to refresh JWT tokens before expiration | <span style="color: green;">Done</span> |
| FR5 | Allow managers to invite new users (managers or carriers) | <span style="color: green;">Done</span> |
| FR6 | Allow invited users to complete registration | <span style="color: green;">Done</span> |
| FR7 | Display a list of available products | <span style="color: green;">Done</span> |
| FR8 | Allow users to search products by name | <span style="color: green;">Done</span> |
| FR9 | Display product details (price, description, stock) | <span style="color: green;">Done</span> |
| FR10 | Allow customers to add products to the cart | <span style="color: green;">Done</span> |
| FR11 | Allow customers to remove products from the cart | <span style="color: green;">Done</span> |
| FR12 | Allow customers to update product quantities in the cart | <span style="color: red;">Not Done</span> |
| FR13 | Automatically calculate cart totals | <span style="color: green;">Done</span> |
| FR14 | Allow customers to place orders | <span style="color: green;">Done</span> |
| FR15 | Validate product stock before confirming orders | <span style="color: green;">Done</span> |
| FR16 | Store user order history | <span style="color: green;">Done</span> |
| FR17 | Allow customers to view order status | <span style="color: green;">Done</span> |
| FR18 | Send order confirmation emails | <span style="color: green;">Done</span> |
| FR19 | Allow carriers to view orders ready for pickup | <span style="color: green;">Done</span> |
| FR20 | Display relevant order information for pickup | <span style="color: green;">Done</span> |
| FR21 | Allow carriers to mark an order as picked up | <span style="color: green;">Done</span> |
| FR22 | Allow managers to add new products | <span style="color: green;">Done</span> |
| FR23 | Allow managers to edit product information | <span style="color: red;">Not Done</span> |
| FR24 | Allow managers to manage product categories | <span style="color: green;">Done</span> |
| FR25 | Allow managers to update product stock levels manually | <span style="color: red;">Not Done</span> |
| FR26 | Allow managers to view and filter all customer orders | <span style="color: red;">Not Done</span> |
| FR27 | Allow managers to create backups of products, categories, and orders | <span style="color: orange;">Partially Done</span> |

---

## Non-Functional Requirements

| ID   | Requirement | Status |
|------|-------------|--------|
| NFR1 | API must be accessible only via HTTPS (TLS 1.2+) in non-local environments | <span style="color: green;">Done</span> |
| NFR2 | Passwords must be hashed using a strong adaptive algorithm (e.g., BCrypt) | <span style="color: green;">Done</span> |
| NFR3 | System must enforce RBAC with deny-by-default authorization | <span style="color: green;">Done</span> |
| NFR4 | System must mitigate common web vulnerabilities (SQLi, XSS, CSRF where applicable) | <span style="color: green;">Done</span> |
| NFR5 | Security-relevant actions must be logged with timestamp and user context | <span style="color: green;">Done</span> |
| NFR6 | Two-factor authentication is mandatory for all users | <span style="color: orange;">Partially Done</span> |
| NFR7 | System must handle at least 100 concurrent requests with <500ms response time | <span style="color: green;">Done</span> |
| NFR8 | Codebase must follow clean architecture principles | <span style="color: green;">Done</span> |
| NFR9 | CI/CD must run automated build, tests, and security checks | <span style="color: green;">Done</span> |
| NFR10 | Application must be containerized with non-root execution | <span style="color: green;">Done</span> |
| NFR11 | API must follow REST conventions with consistent error handling | <span style="color: green;">Done</span> |
| NFR12 | OpenAPI documentation must be maintained for all endpoints | <span style="color: green;">Done</span> |
| NFR13 | Dependency scanning must block critical vulnerabilities | <span style="color: green;">Done</span> |
| NFR14 | Automated tests must ensure ≥80% coverage in core layers | <span style="color: orange;">Partially Done</span> |
| NFR15 | Secrets must not be stored in source code; secret scanning enabled | <span style="color: green;">Done</span> |

---

## Security Requirements

| ID  | Requirement | Status |
|-----|-------------|--------|
| SR1 | Multi-Factor Authentication (MFA) required for authentication | <span style="color: green;">Done</span> |
| SR2 | Lock accounts after 5 failed login attempts with cooldown | <span style="color: red;">Not Done</span> |
| SR3 | Passwords must be at least 12 characters long | <span style="color: green;">Done</span> |
| SR4 | Send email confirmation after registration | <span style="color: green;">Done</span> |
| SR5 | Role-Based Access Control (RBAC) must be enforced | <span style="color: green;">Done</span> |
| SR6 | Sessions expire after inactivity | <span style="color: green;">Done</span> |
| SR7 | Rate limiting must be implemented on entry endpoints | <span style="color: green;">Done</span> |
| SR8 | Sensitive data must be encrypted in transit and at rest | <span style="color: green;">Done</span> |
| SR9 | Passwords must be hashed with bcrypt or equivalent | <span style="color: green;">Done</span> |
| SR10 | GDPR compliance for personal data handling | <span style="color: red;">Not Done</span> |
| SR11 | Input validation must prevent SQL injection and XSS | <span style="color: green;">Done</span> |
| SR12 | Validate all user-submitted data formats | <span style="color: green;">Done</span> |
| SR13 | Validate transactional consistency (orders, stock, totals) | <span style="color: green;">Done</span> |
| SR14 | Secure storage of sensitive logs | <span style="color: green;">Done</span> |
| SR15 | Logs must be backed up in 3 locations (local + 2 cloud) | <span style="color: green;">Done</span> |

# Production

<img src="./images/phase-3/nginx-config.png" alt="Nginx Configuration" width="600">

<img src="./images/phase-3/app.png" alt="App" width="1000">

# Security Requirements and Tests Traceability

| Security Requirement | Test |
|----------------------|------|
| SR1 MFA | |
| SR2 Lockout | |
| SR3 Password policy | |
| SR4 Registration email | |
| SR5 RBAC | |
| SR6 Session Expiry | |
| SR7 Rate limiting | |
| SR8 Encryption at rest and in transit | |
| SR9 Password hashing | |
| SR10 GDPR compliance | |
| SR11 Input validation | |
| SR12 Data format validation | |
| SR13 Secure logs | |
| SR14 Log backup | |