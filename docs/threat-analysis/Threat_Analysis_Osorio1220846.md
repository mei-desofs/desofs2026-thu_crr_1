# STRIDE Threat Analysis

## Invite New Users (managers and carriers)

| STRIDE| Identified Threats|
|-------|-------------------|
| **Spoofing** | **Spoofing Threat 1:** An attacker could impersonate an authorized manager and send invitation requests if authentication or session validation is weak, leading to unauthorized user invitations. |
| **Tampering** | **Tampering Threat 1:** An attacker could manipulate the invited user’s role in the request if the backend does not validate which roles the authenticated manager is allowed to assign. |
| **Repudiation** | **Repudiation Threat 1:** A manager could deny having sent an invitation if invitation actions (email, role, timestamp, actor identity) are not properly logged and audited. |
| **Information Disclosure** | **Information Disclosure Threat 1:** The system could expose whether an email is already registered (e.g., through error messages), allowing attackers to enumerate valid accounts. **Information Disclosure Threat 2:** The invitation token embedded in the email link could be exposed through server logs, browser history, or email forwarding, allowing a third party to redeem the link before the intended recipient. |
| **Denial of Service** | **Denial of Service Threat 1:** An attacker could repeatedly call the invite endpoint, triggering excessive invitation emails and potentially exhausting backend or Supabase rate limits. |
| **Elevation of Privilege** | **Elevation of Privilege Threat 1:** A user without permission to access the invite endpoint could send invitation requests due to missing or weak access control. **Elevation of Privilege Threat 2:** A user could assign roles they are not authorized to assign if role assignment rules are not enforced on the backend. |


## Confirm Invite

| STRIDE| Identified Threats|
|-------|-------------------|
| **Spoofing** | **Spoofing Threat 1:** An attacker could reuse a valid invitation token in the callback endpoint to complete the registration process impersonating the intended recipient, if the token is not single-use or properly bound to the invited email.|
| **Tampering** | **Tampering Threat 1:** An attacker could tamper with the webhook payload (e.g., email, role, supabase id) sent from Supabase to the backend if the webhook request integrity is not verified, leading to the creation of a user with manipulated data in the internal database. |
| **Repudiation** | **Repudiation Threat 1:** A user could deny having completed the registration if the confirmation event (token redemption, timestamp, IP, user agent) is not properly logged both at the callback and webhook levels. |
| **Information Disclosure** | **Information Disclosure Threat 1:** Sensitive information of user (e.g., email, role) in the webhook payload could be exposed if the communication between Supabase and the backend is not encrypted or if error responses leak internal details. |
| **Denial of Service** | **Denial of Service Threat 1:** An attacker could repeatedly trigger the webhook endpoint with forged or replayed payloads, exhausting backend resources or causing duplicate user creation attempts in the internal database. |
| **Elevation of Privilege** | **Elevation of Privilege Threat 1:** An attacker could forge a webhook request to the backend endpoint, creating a user with an arbitrary role in the internal database, if the webhook origin is not properly authenticated (e.g., via secret validation or HMAC signature). |


## Reset Password

| STRIDE| Identified Threats|
|-------|-------------------|
| **Spoofing** | **Spoofing Threat 1:** An attacker could request a password reset on behalf of a legitimate user by submitting their email address, initiating an unsolicited reset flow without the user's knowledge. |
| **Tampering** | **Tampering Threat 1:** An attacker could tamper with the reset request payload (e.g., replacing the target email) if the request is not properly validated server-side, triggering a reset for a different account than intended. |
| **Repudiation** | **Repudiation Threat 1:** A user could deny having requested a password reset if the reset request event (email, timestamp, IP) is not properly logged at the backend level before forwarding to Supabase. |
| **Information Disclosure** | **Information Disclosure Threat 1:** The system could reveal whether an email is registered or not through different responses to reset requests, allowing an attacker to enumerate valid accounts. |
| **Denial of Service** | **Denial of Service Threat 1:** An attacker could repeatedly submit reset requests for the same or different email addresses, exhausting backend resources, Supabase rate limits or flooding target users with unsolicited reset emails. |
| **Elevation of Privilege** | **Elevation of Privilege Threat 1:** An attacker could exploit the reset flow to gain unauthorized access to another user's account if the reset token is not properly validated or bound to the requesting email by Supabase. |

## View the Contents of the Shopping Cart

| STRIDE| Identified Threats|
|-------|-------------------|
| **Spoofing** | **Spoofing Threat 1:** An attacker could impersonate a legitimate customer and access their shopping cart if they stole or forged a session token to gain access. |
| **Tampering** | **Tampering Threat 1:** An attacker could tamper with the cart retrieval request (e.g., changing the customer ID) to access or modify cart data belonging to another customer if the backend does not properly validate that the authenticated user is authorized to access the specified cart. |
| **Repudiation** | **Repudiation Threat 1:** A customer could deny having accessed their shopping cart if cart access events (customer ID, timestamp, IP) are not properly logged at the backend level. |
| **Information Disclosure** | **Information Disclosure Threat 1:** An attacker could access another customer's shopping cart and view its contents if proper access controls are not enforced on the cart retrieval endpoint. |
| **Denial of Service** | **Denial of Service Threat 1:** An attacker could repeatedly access the cart retrieval endpoint with valid or forged credentials, exhausting backend resources or causing performance degradation for legitimate users. |
| **Elevation of Privilege** | **Elevation of Privilege Threat 1:** An attacker could gain unauthorized access to cart contents beyond their permissions if access control is not properly enforced. |