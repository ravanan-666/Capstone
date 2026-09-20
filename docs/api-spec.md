# DjMart API Specification (v1)

All REST API endpoints are versioned under `/api/v1/...`.

## Standard Response Format

### Success Response
```json
{
  "success": true,
  "data": { ... },
  "error": null
}
```

### Error Response
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed on the submitted data",
    "fieldErrors": {
      "email": "Email address already registered"
    }
  }
}
```

## Standard HTTP Status Codes
- `200 OK`: Request succeeded.
- `201 Created`: Resource successfully created (e.g., registration, product listing, order placement).
- `400 Bad Request`: Input validation failure or malformed payload.
- `401 Unauthorized`: Missing or invalid session authentication.
- `403 Forbidden`: Authenticated user does not possess required role (e.g. Buyer accessing Seller route).
- `404 Not Found`: Resource does not exist.
- `409 Conflict`: Business state conflict (e.g., duplicate unique record or insufficient stock).
- `500 Internal Server Error`: Server failure (stack traces concealed).

## Core Endpoints Overview
- `GET /api/v1/health` — System and database health status
- `POST /api/v1/auth/register` — User registration (BUYER or SELLER)
- `POST /api/v1/auth/login` — Authentication and session initiation
- `POST /api/v1/auth/logout` — Invalidate session
- `GET /api/v1/products` — Product catalog with filter and keyword search
- `GET /api/v1/products/{id}` — Detailed product listing with reviews
- `POST /api/v1/cart` — Add or update item in buyer cart
- `GET /api/v1/cart` — View current cart contents and total
- `POST /api/v1/checkout` — Convert cart to confirmed order
- `GET /api/v1/orders` — Order history
- `POST /api/v1/reviews` — Submit review for purchased product
- `POST /api/v1/chat` — AI domain chatbot proxy
