# Rate Limiting Policy

## Rate Limit Types

The application will use two types of rate limiting.

| Type                     | Description                                              |
| ------------------------ | -------------------------------------------------------- |
| IP-based rate limiting   | Limits requests based on the client IP address           |
| User-based rate limiting | Limits requests based on the authenticated user identity |

IP-based rate limiting will be used mainly for anonymous or unauthenticated endpoints, where the user identity may not be available.

User-based rate limiting will be used for authenticated actions, where the application can reliably identify the user making the request.

## Rate Limit Rules

### Authentication and Account Management

| Rule              | Scope |      Limit | Window   |
| ----------------- | ----- | ---------: | -------- |
| `login`           | IP    | 5 requests | 1 minute |
| `register`        | IP    | 5 requests | 1 minute |
| `logout`          | IP    | 5 requests | 1 minute |
| `refresh-token`   | IP    | 5 requests | 1 minute |
| `password-update` | IP    | 5 requests | 1 hour   |
| `invite`          | User  | 5 requests | 1 minute |

### Multi-Factor Authentication

| Rule                   | Scope |       Limit | Window   |
| ---------------------- | ----- | ----------: | -------- |
| `mfa-enroll`           | User  |  5 requests | 1 hour   |
| `mfa-verify`           | User  | 10 requests | 1 minute |
| `mfa-challenge`        | IP    | 10 requests | 1 minute |
| `mfa-unenroll`         | User  |  5 requests | 1 hour   |
| `mfa-challenge-verify` | IP    | 10 requests | 1 minute |
| `mfa-challenge-enroll` | User  | 10 requests | 1 minute |

### Product Operations

| Rule              | Scope |       Limit | Window   |
| ----------------- | ----- | ----------: | -------- |
| `create-product`  | USER  | 10 requests | 1 hour   |
| `list-products`   | IP    | 60 requests | 1 minute |
| `search-products` | IP    | 60 requests | 1 minute |
| `get-product` | IP | 60 requests | 1 minute |
| `update-product-stock` | User | 20 requests | 1 minute |
| `update-product` | User | 20 requests | 1 minute |

### Cart Operations

| Rule                  | Scope |       Limit | Window   |
| --------------------- | ----- | ----------: | -------- |
| `add-item-to-cart`    | User  | 20 requests | 1 minute |
| `update-item-in-cart` | User  | 35 requests | 1 minute |
| `get-cart-items`      | User  | 20 requests | 1 minute |

### Order Operations

| Rule                          | Scope |       Limit | Window   |
| ----------------------------- | ----- | ----------: | -------- |
| `create-order`                | User  | 20 requests | 1 minute |
| `delete-order`                | User  | 20 requests | 1 minute |
| `customer-list-orders`        | User  | 30 requests | 1 minute |
| `carrier-list-orders`         | User  | 30 requests | 1 minute |
| `carrier-list-pending-orders` | User  | 30 requests | 1 minute |
| `carrier-pickup-order`        | User  | 10 requests | 1 minute |

## Endpoint Coverage

The following endpoint groups are planned to be protected by rate limiting:

| Endpoint / Flow                   | Planned Rule                  |
| --------------------------------- | ----------------------------- |
| `POST /api/auth/register`         | `register`                    |
| `POST /api/auth/login`            | `login`                       |
| `POST /api/auth/logout`           | `logout`                      |
| `POST /api/auth/refresh`          | `refresh-token`               |
| `POST /api/auth/invite`           | `invite`                      |
| Password update flow              | `password-update`             |
| MFA enrollment flow               | `mfa-enroll`                  |
| MFA verification flow             | `mfa-verify`                  |
| MFA challenge flow                | `mfa-challenge`               |
| MFA unenrollment flow             | `mfa-unenroll`                |
| `GET /api/products`               | `list-products`               |
| `GET /api/products/search`        | `search-products`             |
| `GET /api/products/{id}`          | `get-product`                 |
| `POST /api/products`              | `create-product`              |
| Product stock update flow         | `update-product-stock`        |
| Product update flow               | `update-product`              |
| `GET /api/cart`                   | `get-cart-items`              |
| `POST /api/cart/items`            | `add-item-to-cart`            |
| `PUT /api/cart/items/{productId}` | `update-item-in-cart`         |
| `POST /api/orders`                | `create-order`                |
| Customer order listing            | `customer-list-orders`        |
| Carrier order listing             | `carrier-list-orders`         |
| Carrier pending order listing     | `carrier-list-pending-orders` |
| Carrier pickup action             | `carrier-pickup-order`        |
