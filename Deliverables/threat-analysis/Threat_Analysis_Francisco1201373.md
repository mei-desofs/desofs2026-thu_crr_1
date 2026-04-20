# STRIDE Threat Analysis - Cart and Order Endpoints

---

## Add Product To Cart
| STRIDE | Identified Threats |
|--------|-------------------|
| **Spoofing** | **Spoofing Threat 1:** An attacker could use a stolen token/session to add products to a victim's cart on their behalf. |
| **Tampering** | **Tampering Threat 1:** An attacker could manipulate cart payload fields (e.g., quantity, product reference) to force invalid cart states if server-side validation is weak.<br>**Tampering Threat 2:** If request integrity is not enforced, client-modified values could be accepted instead of trusted server values. |
| **Repudiation** | **Repudiation Threat 1:** A user could deny having added specific items if cart mutation events (user ID, timestamp, IP, product/quantity) are not logged. |
| **Information Disclosure** | **Information Disclosure Threat 1:** Verbose API errors could reveal valid product identifiers, stock logic, or internal cart rules useful for abuse. |
| **Denial of Service** | **Denial of Service Threat 1:** Attackers could flood cart-add requests to create high write load and degrade cart service responsiveness. |
| **Elevation of Privilege** | **Elevation of Privilege Threat 1:** Weak authorization checks could allow a user to add items to carts outside their ownership scope. |

---

## Update Cart Item Quantity
| STRIDE | Identified Threats |
|--------|-------------------|
| **Spoofing** | **Spoofing Threat 1:** An attacker with stolen credentials/token could update quantities in another user's active cart. |
| **Tampering** | **Tampering Threat 1:** An attacker could send out-of-range or malformed quantity values to break business constraints if validation is insufficient.<br>**Tampering Threat 2:** Concurrent update races could be exploited to force inconsistent quantity states. |
| **Repudiation** | **Repudiation Threat 1:** A user could deny quantity changes if update operations are not fully audited with before/after values. |
| **Information Disclosure** | **Information Disclosure Threat 1:** Error messages from quantity update validation could expose internal inventory or pricing behavior. |
| **Denial of Service** | **Denial of Service Threat 1:** Repeated high-frequency quantity updates could overload cart validation and persistence layers. |
| **Elevation of Privilege** | **Elevation of Privilege Threat 1:** Improper authorization could allow updating quantities for cart items not owned by the authenticated user. |

---

## Remove Product From Cart
| STRIDE | Identified Threats |
|--------|-------------------|
| **Spoofing** | **Spoofing Threat 1:** Stolen session/token abuse could allow unauthorized product removals in victim carts. |
| **Tampering** | **Tampering Threat 1:** Manipulating path parameter values (productId) could trigger unintended removals if ownership checks are weak.<br>**Tampering Threat 2:** Remove/update race conditions could produce inconsistent cart state when concurrency controls are missing. |
| **Repudiation** | **Repudiation Threat 1:** Users could deny remove actions if delete events are not logged with actor identity and item details. |
| **Information Disclosure** | **Information Disclosure Threat 1:** Distinct remove responses (exists vs not found vs unauthorized) could leak cart composition details. |
| **Denial of Service** | **Denial of Service Threat 1:** Attackers could spam remove operations to disrupt checkout flow and degrade cart service availability. |
| **Elevation of Privilege** | **Elevation of Privilege Threat 1:** Broken access checks could allow removing products from another user's cart by guessing identifiers. |

---

## Place Order
| STRIDE | Identified Threats |
|--------|-------------------|
| **Spoofing** | **Spoofing Threat 1:** An attacker could place orders using a hijacked user session/token, impersonating a legitimate customer. |
| **Tampering** | **Tampering Threat 1:** Attackers could tamper with order/cart values (totals, price-related fields) in transit payloads if server-side recomputation is incomplete.<br>**Tampering Threat 2:** Missing idempotency/atomic controls could allow duplicate order creation through replay or rapid re-submit. |
| **Repudiation** | **Repudiation Threat 1:** A customer could deny placing an order if order creation events and payment-related decision logs are incomplete. |
| **Information Disclosure** | **Information Disclosure Threat 1:** Order API responses or logs could expose sensitive purchase details when not properly minimized/protected. |
| **Denial of Service** | **Denial of Service Threat 1:** Flooding checkout requests could overload order orchestration, pricing, and persistence services. |
| **Elevation of Privilege** | **Elevation of Privilege Threat 1:** Authorization flaws could allow placing orders against carts not owned by the authenticated user. |
