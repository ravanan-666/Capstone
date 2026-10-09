# DJ Mart REST API Specification

All RESTful JSON endpoints are accessible under `/api/...` and `/api/v1/...`.

---

## 1. Standard Response Format

Every API endpoint serializes responses wrapped in the unified `ApiResponse<T>` envelope:

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "errorCode": null
}
```

### Error Response
```json
{
  "success": false,
  "message": "Detailed error message",
  "errorCode": "ERROR_CODE",
  "error": {
    "code": "ERROR_CODE",
    "message": "Detailed error message",
    "fieldErrors": {
      "field": "Specific field validation message"
    }
  }
}
```

---

## 2. HTTP Status Code Conventions

| Status Code | Meaning | When Returned |
|---|---|---|
| `200 OK` | Success | Successful retrieval, modification, or cancellation |
| `201 Created` | Resource Created | Successful registration, order placement, or product listing |
| `400 Bad Request` | Validation Failure | Invalid input payload, negative prices, empty cart at checkout |
| `401 Unauthorized` | Authentication Required | Unauthenticated request to protected endpoint |
| `403 Forbidden` | Access Denied | Role permission violation (e.g. Buyer attempting Seller actions) |
| `404 Not Found` | Resource Missing | Non-existent product, order, or user ID |
| `409 Conflict` | Business State Conflict | Duplicate email registration, insufficient product stock |
| `429 Too Many Requests`| Rate Limit Exceeded | AI chat rate limit surpassed (>10 messages/minute) |
| `500 Internal Server Error` | Server Exception | Uncaught runtime error (stack traces hidden from client) |

---

## 3. Endpoints Directory

### Authentication (`/api/v1/auth/*`)
- `POST /api/v1/auth/register` — Register a new account (`name`, `email`, `password`, `role`). Returns `201`.
- `POST /api/v1/auth/login` — Authenticate and start secure session (`email`, `password`). Returns `200`.
- `POST /api/v1/auth/logout` — Invalidate user session. Returns `200`.
- `GET  /api/v1/auth/me` — Inspect current session user profile. Returns `200` or `401`.

### Product Catalog (`/api/v1/products/*`)
- `GET  /api/v1/products` — Paginated catalog search with query params:
  - `query`: Keyword search for name or description
  - `category`: Category filter (e.g., `Electronics`, `Fashion`, `Home & Kitchen`)
  - `sort`: Sorting criteria (`price_asc`, `price_desc`, `newest`, `name_asc`)
  - `page`: 1-based page index (default: `1`)
  - `size`: Page size (default: `12`)
- `GET  /api/v1/products/{id}` — Fetch single product detail by ID with seller info and reviews.
- `POST /api/v1/products` — Create product listing (`SELLER` or `ADMIN`).
- `PUT  /api/v1/products/{id}` — Update product listing (owner `SELLER` or `ADMIN`).
- `DELETE /api/v1/products/{id}` — Delete product listing (owner `SELLER` or `ADMIN`).

### Shopping Cart (`/api/v1/cart/*`)
- `GET    /api/v1/cart` — Retrieve buyer active cart items and server-computed total.
- `POST   /api/v1/cart/items` — Add product to cart (`productId`, `quantity`). Stock is validated.
- `PUT    /api/v1/cart/items/{id}` — Update item quantity (`quantity`). Cannot exceed available stock.
- `DELETE /api/v1/cart/items/{id}` — Remove item from cart.

### Orders & Checkout (`/api/v1/orders/*`, `/api/v1/checkout`)
- `POST /api/v1/checkout` — Convert cart into placed order within atomic transaction (`shippingAddress`). Returns `201`.
- `GET  /api/v1/orders` — List order history for authenticated buyer (or all orders for admin).
- `GET  /api/v1/orders/{id}` — Retrieve full details for specific order ID.
- `POST /api/v1/orders/{id}/cancel` — Cancel pending order and restore stock (`BUYER`).
- `POST /api/v1/orders/{id}/status` — Update order status (`SELLER` for relevant orders, `ADMIN`).

### Reviews (`/api/v1/reviews/*`)
- `GET  /api/v1/reviews?productId={id}` — Get all reviews and computed average rating.
- `POST /api/v1/reviews` — Submit review (`productId`, `rating`, `comment`). Requires verified purchase.

### AI Customer Concierge (`/api/chat` and `/api/v1/chat`)
- `POST /api/chat` — Send query to AI Concierge (`message`). Rate limited to 10 requests/minute per session.

### Health Check (`/api/v1/health`)
- `GET /api/v1/health` — Returns system status and database connectivity.
