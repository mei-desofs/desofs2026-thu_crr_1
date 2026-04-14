# STRIDE Threat Analysis – Product Endpoints

---

## Get All Products
| STRIDE | Identified Threats                                                                                                                                                                |
|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Spoofing** | **Spoofing Threat 1:** An attacker could forge request headers (e.g., X-User-Role) to impersonate a different user type and attempt to access product data with elevated context. |
| **Tampering** | **Tampering Threat 1:** An attacker could manipulate query parameters (e.g., pagination or filter fields) to retrieve unintended data or cause unexpected backend behaviour.      |
| **Repudiation** | **Repudiation Threat 1:** A user could deny having browsed specific products if product listing access is not logged, hindering audit trails.                                     |
| **Information Disclosure** | **Information Disclosure Threat 1:** The response may expose internal fields (e.g., supplier cost, internal IDs) that should not be visible to anonymous or low-privilege users.  |
| **Denial of Service** | **Denial of Service Threat 1:** An unauthenticated attacker could flood the endpoint with requests, exhausting backend resources since no authentication is required.             |
| **Elevation of Privilege** | **Elevation of Privilege Threat 1:** A user could attempt to access manager-only product fields by manipulating the request if field-level authorization is not enforced.         |

---

## Get Product By Name
| STRIDE | Identified Threats                                                                                                                                                                                   |
|--------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Spoofing** | **Spoofing Threat 1:** An attacker could spoof a legitimate user session to perform searches and harvest the product catalogue systematically.                                                       |
| **Tampering** | **Tampering Threat 1:** An attacker could inject SQL or NoSQL operators via the productName parameter if input is not properly sanitized, leading to injection attacks.                              |
| **Repudiation** | **Repudiation Threat 1:** Malicious or abusive search queries could go undetected if search requests are not logged with user context.                                                               |
| **Information Disclosure** | **Information Disclosure Threat 1:** Unfiltered search results may expose draft or deactivated products not intended for public visibility.                                                          |
| **Denial of Service** | **Denial of Service Threat 1:** An attacker could submit a high volume of search requests with varying inputs to exhaust database query capacity.                                                    |
| **Elevation of Privilege** | **Elevation of Privilege Threat 1:** A low-privilege user could craft search queries to discover products outside their permitted scope if search results are not filtered by authorization context. |

---

## Create Product

| STRIDE | Identified Threats                                                                                                                                                                     |
|--------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Spoofing** | **Spoofing Threat 1:** An attacker could forge or steal a Manager JWT to impersonate a manager and create fraudulent products in the catalogue.                                        |
| **Tampering** | **Tampering Threat 1:** A malicious manager or intercepted request could inject unexpected field values to corrupt the product catalogue.                                              |
| **Repudiation** | **Repudiation Threat 1:** A manager could deny having created a fraudulent or erroneous product if creation events are not logged with the responsible user identity and timestamp.    |
| **Information Disclosure** | **Information Disclosure Threat 1:** Validation error responses could reveal internal data model structure (e.g., field names, constraints, database error messages).                  |
| **Denial of Service** | **Denial of Service Threat 1:** An attacker with a valid Manager token could create a large number of products in rapid succession, polluting the catalogue and exhausting storage.    |
| **Elevation of Privilege** | **Elevation of Privilege Threat 1:** A Customer or Carrier user could attempt to call this endpoint directly, bypassing the UI, if endpoint authorization is not enforced server-side. |

---

## Update Product

| STRIDE | Identified Threats |
|--------|-------------------|
| **Spoofing** | **Spoofing Threat 1:** An attacker could forge a Manager token to gain write access and modify product information such as price or stock levels. |
| **Tampering** | **Tampering Threat 1:** A malicious actor could manipulate the product ID in the URL path to modify a product other than the intended one (mass assignment or IDOR). |
| **Repudiation** | **Repudiation Threat 1:** A manager could deny having altered a product's price or stock if update events are not logged with the previous and new values alongside the responsible user. |
| **Information Disclosure** | **Information Disclosure Threat 1:** The response body after a successful PATCH could expose fields beyond what was updated, leaking internal product data to the requester. |
| **Denial of Service** | **Denial of Service Threat 1:** Rapid repeated PATCH requests to the same or multiple products could lock database rows or overwhelm the update pipeline if no rate limiting is applied to write operations. |
| **Elevation of Privilege** | **Elevation of Privilege Threat 1:** A Customer or Carrier could attempt to call this endpoint directly to alter product prices or stock in their favour if server-side role validation is not enforced on every request. |