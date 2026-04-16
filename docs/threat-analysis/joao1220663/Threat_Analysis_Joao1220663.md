# STRIDE Threat Analysis – Product Endpoints

---

## Get All Products
| STRIDE | Identified Threats                                                                                                                                                                |
|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Spoofing** | **Spoofing Threat 1:** An attacker could spoof IP addresses (via proxies or botnets) to bypass rate limiting or IP-based protections. |
| **Tampering** | **Tampering Threat 1:** An attacker could manipulate query parameters (e.g., pagination or filter fields) to retrieve unintended data or cause unexpected backend behaviour.      |
| **Repudiation** | **Repudiation Threat 1:** A user could deny having browsed specific products if product listing access is not logged, hindering audit trails.                                     |
| **Information Disclosure** | **Information Disclosure Threat 1:** The response may expose internal fields (e.g., supplier cost, internal IDs) that should not be visible to anonymous or low-privilege users.  |
| **Denial of Service** | **Denial of Service Threat 1:** An unauthenticated attacker could flood the endpoint with requests, exhausting backend resources since no authentication is required.             |
| **Elevation of Privilege** | **Elevation of Privilege Threat 1:** If the endpoint reuses internal logic without proper filtering, it may unintentionally expose data intended only for privileged contexts.         |

---

## Get Product By Name
| STRIDE | Identified Threats                                                                                                                                                                                   |
|--------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Spoofing** | **Spoofing Threat 1:** An attacker may use proxies/botnets to rotate IPs and bypass rate limiting or detection mechanisms.                                                       |
| **Tampering** | **Tampering Threat 1:** An attacker could inject SQL or NoSQL operators via the productName parameter if input is not properly sanitized, leading to injection attacks.                              |
| **Repudiation** | **Repudiation Threat 1:** Malicious or abusive search queries could go undetected if search requests are not logged with user context.                                                               |
| **Information Disclosure** | **Information Disclosure Threat 1:** Unfiltered search results may expose draft or deactivated products not intended for public visibility.                                                          |
| **Denial of Service** | **Denial of Service Threat 1:** An attacker could submit a high volume of search requests with varying inputs to exhaust database query capacity.                                                    |
| **Elevation of Privilege** | **Elevation of Privilege Threat 1:** If the search endpoint reuses internal services without proper filtering, it may return fields or records normally restricted to higher-privilege contexts. |

---

## Create Product

| STRIDE | Identified Threats                                                                                                                                                                      |
|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Spoofing** | **Spoofing Threat 1:** An attacker could forge or steal a Manager JWT to impersonate a manager and create fraudulent products in the catalogue.                                         |
| **Tampering** | **Tampering Threat 1:** A malicious manager or intercepted request could inject unexpected field values to corrupt the product catalogue.                                               |
| **Repudiation** | **Repudiation Threat 1:** A manager could deny having created a fraudulent or erroneous product if creation events are not logged with the responsible user identity and timestamp.     |
| **Information Disclosure** | **Information Disclosure Threat 1:** Validation error responses could reveal internal data model structure (e.g., field names, constraints, database error messages).                   |
| **Denial of Service** | **Denial of Service Threat 1:** An attacker with a valid Manager token could create a large number of products in rapid succession, polluting the catalogue and exhausting storage.     |
| **Elevation of Privilege** | **Elevation of Privilege Threat 1:** A non-manager user could attempt to call this endpoint directly and gain unauthorized access if server-side authorization is not properly enforced |

---

## Update Product

| STRIDE | Identified Threats                                                                                                                                                                                                     |
|--------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Spoofing** | **Spoofing Threat 1:** An attacker could forge a Manager token to gain write access and modify product information such as price or stock levels.                                                                      |
| **Tampering** | **Tampering Threat 1:** A malicious actor could manipulate the product ID in the URL path to modify a product other than the intended one (mass assignment or IDOR).                                                   |
| **Repudiation** | **Repudiation Threat 1:** A manager could deny having altered a product's price or stock if update events are not logged with the previous and new values alongside the responsible user.                              |
| **Information Disclosure** | **Information Disclosure Threat 1:** The response body after a successful PATCH could expose fields beyond what was updated, leaking internal product data to the requester.                                           |
| **Denial of Service** | **Denial of Service Threat 1:** Rapid repeated PATCH requests to the same or multiple products could lock database rows or overwhelm the update pipeline if no rate limiting is applied to write operations.           |
| **Elevation of Privilege** | **Elevation of Privilege Threat 1:** A non-manager user could attempt to call this endpoint directly to alter product prices or stock in their favour if server-side role validation is not enforced on every request. |