A secure E-Commerce REST API using Java Spring Boot that supports product browsing and order placement with role-based access control using OAuth2 Resource Server implementation.

## Features

### Authentication & Authorization
- OAuth2-based JWT authentication
- Two roles: ADMIN and CUSTOMER
- Role-based access control enforced at service layer
- Secure token generation and validation

### Category Management (Admin Only)
- Create, update, delete categories
- List all categories (public access)
- Service-level role validation

### Product Management
- Admin: Create, update, delete products
- Public: List all products, get by category
- Product availability validation

### Order Management
- Customer: Place orders, view own orders
- Admin: View all orders, update order status
- Java Streams for order calculations
- Stock management and validation

### Additional Features
- Global exception handling
- Input validation
- DTO pattern (no direct entity exposure)
- Clean layered architecture

# Setup Instructions 

## Prerequisites 
-Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Git

### Step 1: Clone the Repository
```bash
git clone https://github.com/Tanmay-Anand/secure-e-commerce 
cd secure-E-Commerce
```

### Step 2: Database Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE secure_ecommerce;
CREATE USER postgres2 WITH PASSWORD 'postgres2';
GRANT ALL PRIVILEGES ON DATABASE secure_ecommerce TO postgres2;
```

For custom username and password, update `application.yml`
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/secure_ecommerce
    username: postgres2
    password: postgres2
```

### Step 3: Build the Project
```bash
mvn clean install
```

### Step 4: Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Step 5: Create Admin and customer in pgAdmin 4
```sql
-- Admin user (username: admin, password: password123)

INSERT INTO users (username, password, email, role) VALUES
('admin', '$2a$10$xqL5K3qKZ5QK5K5K5K5K5OqxGxB7X3.3YxXhYZ5w5OqxGxB7X3.3Y', 'admin@example.com', 'ADMIN');

-- Customer user (username: customer, password: password123)

INSERT INTO users (username, password, email, role) VALUES
('customer1', '$2a$10$xqL5K3qKZ5QK5K5K5K5K5OqxGxB7X3.3YxXhYZ5w5OqxGxB7X3.3Y', 'customer1@example.com', 'CUSTOMER');
```

## OAuth2 Token Generation

### Overview
This API uses JWT (JSON Web Tokens) for authentication. Tokens must be included in the `Authorization` header for protected endpoints.

### OAuth2 Implementation Note
This API implements OAuth2 using JWT tokens as the authentication mechanism. The application acts as an OAuth2 Resource Server that validates JWT Bearer tokens.

**OAuth2 Flow:**
1. Client sends credentials to `/api/auth/login` (Token Endpoint)
2. Server validates credentials and generates JWT access token
3. Token contains OAuth2 claims: `sub` (subject/username), `role` (authority), `exp` (expiration)
4. Client includes token in `Authorization: Bearer {token}` header
5. Resource Server validates token and enforces role-based access control

**OAuth2 Components in this implementation:**
- **Resource Server**: The entire Spring Boot application
- **Access Token**: JWT with HS256 signature
- **Token Validation**: JwtAuthenticationFilter validates signatures and expiration
- **Authorization**: Role-based access control enforced at service layer

#### Sample 1: Login as Admin

**Method:** `POST`  
**URL:** `http://localhost:8080/api/auth/login`  
**Body (raw JSON):**
```json
{
  "username": "admin",
  "password": "password123"
}
```
**Note:** Save the returned token for subsequent requests

---

#### Sample 2: Login as Customer

**Method:** `POST`  
**URL:** `http://localhost:8080/api/auth/login`  
**Body (raw JSON):**
```json
{
  "username": "customer1",
  "password": "password123"
}
```
**Note:** Save the returned token for subsequent requests

---

### Category Management Samples

#### Sample 3: Create Category (as Admin) 

**Method:** `POST`  
**URL:** `http://localhost:8080/api/categories`  
**Headers:**
- `Authorization: Bearer {ADMIN_TOKEN}`
- `Content-Type: application/json`

**Body:**
```json
{
  "name": "Electronics",
  "description": "Electronic devices and accessories"
}
```
**Expected Result:** `201 Created`

---

#### Sample 4: Try Create Category (as Customer) 

**Method:** `POST`  
**URL:** `http://localhost:8080/api/categories`  
**Headers:**
- `Authorization: Bearer {CUSTOMER_TOKEN}`
- `Content-Type: application/json`

**Body:**
```json
{
  "name": "Books",
  "description": "Books and novels"
}
```
**Expected Result:** `403 Forbidden` - Only ADMIN users can create categories

---

#### Sample 5: List Categories (Public Access) 

**Method:** `GET`  
**URL:** `http://localhost:8080/api/categories`  
**Headers:** None required  
**Expected Result:** `200 OK` with list of all categories

---

### Product Management Samples

#### Sample 6: Create Product (as Admin) 

**Method:** `POST`  
**URL:** `http://localhost:8080/api/products`  
**Headers:**
- `Authorization: Bearer {ADMIN_TOKEN}`
- `Content-Type: application/json`

**Body:**
```json
{
  "name": "iPhone 15",
  "description": "Latest iPhone model",
  "price": 999.99,
  "stock": 100,
  "categoryId": 1
}
```
**Expected Result:** `201 Created`

---

#### Sample 7: Create Another Product (as Admin) 

**Method:** `POST`  
**URL:** `http://localhost:8080/api/products`  
**Headers:**
- `Authorization: Bearer {ADMIN_TOKEN}`
- `Content-Type: application/json`

**Body:**
```json
{
  "name": "MacBook Pro",
  "description": "Professional laptop",
  "price": 2499.99,
  "stock": 50,
  "categoryId": 1
}
```
**Expected Result:** `201 Created`

---

#### Sample 8: Try Create Product (as Customer) 

**Method:** `POST`  
**URL:** `http://localhost:8080/api/products`  
**Headers:**
- `Authorization: Bearer {CUSTOMER_TOKEN}`
- `Content-Type: application/json`

**Body:**
```json
{
  "name": "iPad",
  "description": "Tablet",
  "price": 599.99,
  "stock": 75,
  "categoryId": 1
}
```
**Expected Result:** `403 Forbidden` - Only ADMIN users can create products

---

## Error Handling

The API returns standard HTTP status codes:

- `200 OK`: Successful GET/PUT/PATCH request
- `201 Created`: Successful POST request
- `204 No Content`: Successful DELETE request
- `400 Bad Request`: Invalid input/validation error
- `401 Unauthorized`: Missing or invalid token
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

### ER Diagram
```
┌─────────────┐
│    users    │
├─────────────┤
│ id (PK)     │
│ username    │
│ password    │
│ email       │
│ role        │
└─────────────┘
       │
       │ 1:N
       ▼
┌─────────────┐
│   orders    │
├─────────────┤
│ id (PK)     │
│ user_id (FK)│
│ total_amount│
│ status      │
│ created_at  │
│ updated_at  │
└─────────────┘
       │
       │ 1:N
       ▼
┌──────────────┐         ┌─────────────┐
│ order_items  │    N:1  │  products   │
├──────────────┤◄────────┤─────────────┤
│ id (PK)      │         │ id (PK)     │
│ order_id (FK)│         │ name        │
│ product_id(FK)         │ description │
│ quantity     │         │ price       │
│ price        │         │ stock       │
└──────────────┘         │category_id  │
                         └─────────────┘
                                │
                                │ N:1
                                ▼
                         ┌─────────────┐
                         │ categories  │
                         ├─────────────┤
                         │ id (PK)     │
                         │ name        │
                         │ description │
                         └─────────────┘
```