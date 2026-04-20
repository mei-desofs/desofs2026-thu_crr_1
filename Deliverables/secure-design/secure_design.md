### Secure Design
The secure design of the TechStore API is grounded in the principles of the Secure Software Development Life Cycle (SSDLC), ensuring that security is considered from the earliest stages of development. The system adopts key security concepts such as defense in depth, least privilege, and secure defaults. This section explains how these principles are applied across the system’s main components and workflows.
#### Authentication and Access Control

* **Role-Based Access Control (RBAC)**: The system uses RBAC to ensure that users can only access resources and perform actions appropriate to their assigned role (Customer, Carrier, Manager/Admin). Each user is assigned a role, which defines their permissions regarding system features and API endpoints. These roles are clearly separated and enforced on the server side.
* **Least Privilege**: Users are granted only the permissions necessary to complete their tasks, minimizing unnecessary access.

#### Data Protection and Privaçy

* **Data Encryption**: All communication between clients and the server is secured using HTTPS/TLS. Sensitive information is stored using secure hashing mechanisms such as bcrypt. Personal data is handled in accordance with GDPR requirements. 
* **Secure Backups**: Critical data, including logs, is backed up in encrypted form across multiple locations, including two online and one offline backup.

#### Input Validation and Output Handling

* **Strict Input Validation**: All incoming requests are validated against strict data contracts. Fields such as name, price, description, and search inputs are verified and sanitized on the server side.
* **Protection Against Injection Threats**: SQL Injection risks are reduced through the use of parameterized queries and ORM frameworks. Cross-site scripting (XSS) is mitigated by restricting inputs to plain text and applying sanitization rules. 
* **Safe Error Responses**: The system avoids exposing sensitive technical details by returning generic error messages, particularly in cases of authentication failures or internal errors.

#### Secure File and OS Operations

* **File Storage Isolation**: Any files created or managed by the application are stored in isolated directories, separate from publicly accessible areas such as the web root, preventing unauthorized access.

#### Logging and Monitoring

* **Audit Logging**: All critical and sensitive actions are logged. These logs are stored locally and are intended to be forwarded to an external monitoring service as part of the deployment process.

