# Entry Points

|ID|Name|Description|Trust Level|
|--|----|-----------|-----------|
|1| HTTPs Port| The API will be only acessible via TLS encrypted HTTPs connections. | Anonymous User, Customer, Carrier, Manager, Invited User |
|2| POST /api/auth/register| The register endpoint allows unregistered users to create a new account as a customer. | Anonymous User |
|3| POST /api/auth/login| The login endpoint allows users to authenticate and obtain a JWT token for subsequent requests. | Anonymous User |
|4| POST /api/auth/logout| The logout endpoint allows authenticated users to invalidate their JWT token and end their session. | Customer, Carrier, Manager |
|5| POST /api/auth/refresh| The refresh endpoint allows authenticated users to obtain a new JWT token before the current one expires. | Customer, Carrier, Manager |
|6| POST /api/auth/invite| The invite endpoint allows managers to invite new users (managers or carriers) to the system. | Manager |
|7| POST /api/auth/confirm-invite| The confirm invite endpoint allows invited users to complete their registration process. | Invited User |
|8| POST /api/auth/password-reset/request | Allows users to request a password reset link via email. | Anonymous User |
|9| GET /api/products| The products endpoint allows users to retrieve a list of available products. | Anonymous User, Customer, Carrier, Manager, Invited User |
|10| GET /api/products/search?productName={name}| The products endpoint allows users to search for products by name. | Anonymous User, Customer, Carrier, Manager, Invited User |
|11| POST /api/products| The products endpoint allows managers to create new products. | Manager |
|12| PATCH /api/products/{id}| The products endpoint allows managers to update existing products. | Manager |
|13| GET /api/cart | The cart endpoint allows customers to view the contents of their shopping cart. | Customer |
|14| POST /api/cart/items | The cart endpoint allows customers to add products to their shopping cart. | Customer |
|15| PUT /api/cart/items/{productId} | The cart endpoint allows customers to update the quantity of a product in their shopping cart. | Customer |
|16| DELETE /api/cart/items/{productId} | The cart endpoint allows customers to remove a product from their shopping cart. | Customer |
|17| POST /api/orders | The orders endpoint allows customers to place a new order based on the contents of their shopping cart. | Customer |
|18| GET /api/orders | The orders endpoint allows customers to view their order history. | Customer |
|19| GET /api/carrier/orders | The carrier orders endpoint allows carriers to view the orders assigned to them for delivery. | Carrier |
|20| PATCH /api/carrier/{orderId}/pickup | The carrier orders endpoint allows carriers to update the status of an order to "picked up". | Carrier |
|21| POST /api/manager/backup | The manager backup endpoint allows managers to create a backup of the products, categories, and orders data. | Manager |
|22| GET /api/auth/me | The auth endpoint allows authenticated users to retrieve their own user information. | Customer, Carrier, Manager |
|23| GET /api/categories | The categories endpoint allows users to retrieve a list of product categories. | Customer |
|24| POST /api/auth/mfa/enroll | Allows authenticated users to enroll an MFA factor. | Customer, Carrier, Manager |
|25| POST /api/auth/mfa/verify | Allows authenticated users to verify MFA enrollment. | Customer, Carrier, Manager |
|26| POST /api/auth/mfa/challenge | Allows users in the MFA login flow to request an MFA challenge. | Anonymous User |
|27| POST /api/auth/mfa/challenge/verify | Allows users in the MFA login flow to verify an MFA challenge. | Anonymous User |
|28| POST /api/auth/mfa/enroll/challenge | Allows authenticated users to create an MFA enrollment challenge. | Customer, Carrier, Manager |
|29| DELETE /api/auth/mfa/{factorId} | Allows authenticated users to unenroll an MFA factor. | Customer, Carrier, Manager |
|30| GET /api/auth/mfa/status | Allows authenticated users to retrieve their MFA enrollment status. | Customer, Carrier, Manager |
|31| POST /api/auth/set-password | Allows users with a valid authorization token to set or update their password. | Invited User, Customer, Carrier, Manager |
|32| POST /api/auth/confirm | Confirms a registered user's email using an access token. | Anonymous User |