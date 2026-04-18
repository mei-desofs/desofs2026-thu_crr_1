### Secure Design
The secure design of our Technology Online Store System is based on the principles of the Secure Software Development Life Cycle (SSDLC), addressing security from the initial design stage. The system integrates security principles such as defence in depth, least privilege, and secure defaults. This section details the application of these principles to the core system components and workflows. 

#### Authentication and Access Control

* **Role-Based Access Control (RBAC)**: The system implements RBAC to ensure that users can only access resources and perform actions that are appropriate for their role (Customer, Carrier, Manager/Admin). Users are assigned roles (User, Manager, Carrier) which determine access to system features and API endpoints. Each role is clearly separated and enforced server-side. 
* **Least Privilege**: Users are granted the minimum permissions necessary to perform their tasks. Each role is only granted permission strictly necessary to perform its tasks.

#### Data Protection and Privaçy

* **Encryption in Transit and at Rest**: All data transmitted between clients and server uses HTTPS/TLS. Sensitive data in the database is securely hashed using bcrypt. Personal information is protected in compliance with GDPR. 
* **Secure Backups**: All critical data, including logs, are backed up in encrypted form to two online and one offline location.

#### Input Validation and Output Handling

* **Whitelisting and Data Type Validation**: All incoming requests are validated using strong data contracts. Fields like name, price, description, and search queries are validated and sanitized server-side.
* **Protection Against Injection Attacks**: SQL Injection is mitigated using parameterized queries and ORM frameworks. XSS risks are addressed by accepting only plain text in inputs and applying sanitization rules. 
* **Error Handling**: The system avoids leaking technical details in API error messages and returns generic messages for authentication or internal errors. 

#### Secure File and OS Operations

* **Directory/File Isolation**: The system is designed to store any files generated or written by the application in isolated directories, separated from the web root and other publicly accessible paths. This prevents unauthorized file access and exposure through unintended routes. 

#### Logging and Monitoring

* **Audit Logging**: The system is designed to log all sensitive operations. These logs are recorded locally and are intended to be forwarded to an external log monitoring service as part of the deployment pipeline. 

### Secure architecture 

Secure architecture is based on the principles of defense in depth, where it must ensure that security methods and principles are implemented at all layers, in the part of the infrastructure and operational processes. 

We will describe, in stages, the steps necessary to implement a secure architecture in the Technology Online Store System, in addition, the inputs and outputs of each request made by users will be detailed and treated.  

### Application Layer

The layer closest to the user, responsible for communication between applications and users. This is where data is converted to a user-understandable format and where applications request network services. Therefore, we will point out which steps we will deal with in this layer: 

* All client-server communications will be encrypted using https/tls.
* Authentication is managed by the Supabase Identity Provider, introducing the OAuth 2.o and OpenId Connect protocols.
* Role-Based Access Control (RBAC) applies policies and restricted access measures with roles, according to the defined roles (user, manager, carrier). 
* Handling of all endpoints to ensure that only correctly formatted and sanitized endpoints are entered. 
* The error messages will be generic, where we avoid exposing sensitive system details. 

