# 🧪 Testing Guide - NexusFlow

## Quick Test Commands

### Run All Tests
```bash
cd server
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=OrderServiceTest
mvn test -Dtest=ProductServiceTest
mvn test -Dtest=OrderControllerTest
```

### Run with Coverage
```bash
mvn test jacoco:report
# Report will be in: target/site/jacoco/index.html
```

---

## Test Coverage Summary

### ✅ Service Layer Tests

#### OrderServiceTest (2 tests)
- ✅ `createOrder_Success` - Happy path
- ✅ `createOrder_InsufficientStock` - Edge case

#### ProductServiceTest (8 tests)
- ✅ `getAllProducts_Success`
- ✅ `getProductById_Success`
- ✅ `getProductById_NotFound`
- ✅ `createProduct_Success`
- ✅ `updateProduct_Success`
- ✅ `updateProduct_NotFound`
- ✅ `deleteProduct_Success`
- ✅ `deleteProduct_NotFound`

### ✅ Controller Layer Tests

#### OrderControllerTest (3 tests)
- ✅ `createOrder_Success`
- ✅ `createOrder_ValidationError_EmptyItems`
- ✅ `getMyOrders_Success`

**Total: 13 tests**

---

## Manual API Testing

### 1. Start the Application
```bash
docker-compose up --build
```

### 2. Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### 3. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

Save the JWT token from response!

### 4. Test Validation (Should Fail)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"productId": 1, "quantity": 0}
    ]
  }'
```

Expected: 400 Bad Request with validation error

### 5. Test Health Check
```bash
curl http://localhost:8080/actuator/health
```

Expected: `{"status":"UP"}`

---

## Using Swagger UI (Recommended!)

1. Open: http://localhost:8080/swagger-ui.html
2. Click "Authorize" button
3. Paste JWT token (without "Bearer ")
4. Test all endpoints interactively!

---

## Testing Optimistic Locking

Run this test to simulate concurrent updates:

```bash
# Terminal 1 & 2 simultaneously:
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [{"productId": 1, "quantity": 99}]
  }'
```

One should succeed, the other should get 409 Conflict.

---

## Expected Test Output

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.nexusflow.server.service.OrderServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.nexusflow.server.service.ProductServiceTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.nexusflow.server.controller.OrderControllerTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
```

✅ All tests passing!
