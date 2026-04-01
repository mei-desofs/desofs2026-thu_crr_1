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
|8| POST /api/auth/reset-password| The reset password endpoint allows users to request a password reset link via email. | Anonymous User |
|9| GET /api/customer/profile| The customer profile endpoint allows authenticated customers to retrieve their profile information. | Customer |
|10| PUT /api/customer/profile| The customer profile endpoint allows authenticated customers to update their profile information. | Customer |
|11| GET /api/products| The products endpoint allows users to retrieve a list of available products. | Anonymous User, Customer, Carrier, Manager, Invited User |
|12| GET /api/products/search?productName={name}| The products endpoint allows users to search for products by name. | Anonymous User, Customer, Carrier, Manager, Invited User |
|13| GET /api/products?category={category}| The products endpoint allows users to search for products by category. | Anonymous User, Customer, Carrier, Manager, Invited User |
|14| POST /api/products| The products endpoint allows managers to create new products. | Manager |
|15| PUT /api/products/{id}| The products endpoint allows managers to update existing products. | Manager |
|16| PATCH /api/products/{id}/status| The products endpoint allows managers to update the status of a product. | Manager |
|17| PATCH /api/products/{id}/stock| The products endpoint allows managers to update the stock quantity of a product. | Manager |
|18| GET /api/categories| The categories endpoint allows users to retrieve a list of product categories. | Anonymous User, Customer, Carrier, Manager, Invited User |
|19| POST /api/categories| The categories endpoint allows managers to create new product categories. | Manager |
|20| PUT /api/categories/{id}| The categories endpoint allows managers to update existing product categories. | Manager |
|21| GET /api/cart | The cart endpoint allows customers to view the contents of their shopping cart. | Customer |
|22| POST /api/cart/items | The cart endpoint allows customers to add products to their shopping cart. | Customer |
|23| PUT /api/cart/items/{productId} | The cart endpoint allows customers to update the quantity of a product in their shopping cart. | Customer |
|24| DELETE /api/cart/items/{productId} | The cart endpoint allows customers to remove a product from their shopping cart. | Customer |
|25| POST /api/orders | The orders endpoint allows customers to place a new order based on the contents of their shopping cart. | Customer |
|26| GET /api/orders | The orders endpoint allows customers to view their order history. | Customer |
|27| GET /api/orders/{id} | The orders endpoint allows customers to view the details of a specific order. | Customer |
|28| GET /api/carrier/orders | The carrier orders endpoint allows carriers to view the orders assigned to them for delivery. | Carrier |
|29| PATCH /api/carrier/{orderId}/pickup | The carrier orders endpoint allows carriers to update the status of an order to "picked up". | Carrier |
|30| GET api/reports/sales | The sales reports endpoint allows managers to view sales reports and analytics. | Manager |

