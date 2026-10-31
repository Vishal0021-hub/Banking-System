# Comprehensive Project Bug & Error Report
**Enterprise Digital Banking Platform**
**Date:** October 31, 2026

---

## Executive Summary
A comprehensive security, architecture, and code analysis of the Enterprise Banking System microservices backend and React frontend was conducted. Several critical and high-priority bugs were discovered that directly impact authentication, session persistence, inter-service Feign communication, notification delivery, and database queries.

Below is the detailed catalog of bugs, their root causes, and planned fixes before proceeding with remediation.

---

## Identified Bugs & Vulnerabilities

### 1. [CRITICAL] JWT Claims `userId` ClassCastException / RequiredTypeException
* **Affected Files:**
  * `common-lib/src/main/java/com/banking/common/security/JwtTokenProvider.java`
  * `common-lib/src/main/java/com/banking/common/security/JwtAuthenticationFilter.java`
  * `api-gateway/src/main/java/com/banking/gateway/filter/JwtAuthGatewayFilter.java`
* **Root Cause:**
  When Jackson serializes claims into the JWT token payload, JSON numbers are untyped. When parsing the signed JWT, Jackson deserializes standard integer numbers into `java.lang.Integer`. The code explicitly invoked `claims.get("userId", Long.class)`. In JJWT (`io.jsonwebtoken`), `claims.get(key, requiredType)` enforces strict type compatibility and throws `RequiredTypeException: Expected value of type class java.lang.Long, but found class java.lang.Integer`.
* **Impact:**
  * In `JwtAuthGatewayFilter`: The exception is caught by `catch (Exception e)`, returning HTTP 401 "Invalid token" for every authenticated request passing through the API Gateway.
  * In `JwtAuthenticationFilter`: Downstream services reject valid tokens with HTTP 401 "Invalid or expired token".
  * Authentication fails system-wide for all users with IDs < 2^31.
* **Planned Fix:**
  Safely extract `userId` as `Object` and handle `Number.longValue()` or parse from string representation without throwing exceptions.

---

### 2. [CRITICAL] Missing User Profile Creation & Immediate Logout Loop
* **Affected Files:**
  * `auth-service/src/main/java/com/banking/auth/service/impl/AuthServiceImpl.java`
  * `user-service/src/main/java/com/banking/user/service/impl/UserServiceImpl.java`
  * `user-service/src/main/java/com/banking/user/controller/UserController.java`
  * `api-gateway/src/main/java/com/banking/gateway/filter/JwtAuthGatewayFilter.java`
  * `frontend/src/App.jsx`
* **Root Cause:**
  When a user registers in `auth-service`, a record is saved into `banking_auth.users`. However, `user-service` is never invoked, nor does `user-service` consume the `user-events` Kafka topic. Consequently, no `UserProfile` is created in `banking_users.user_profiles`. When a user logs in, `App.jsx` immediately issues a session validation call to `GET /api/v1/users/me`. Because no profile exists, `UserServiceImpl.getProfile` throws `ResourceNotFoundException` (HTTP 404). In `App.jsx`, this 404 triggers the catch block: `logout();`, instantly clearing the session and kicking the user back to the login screen.
* **Impact:**
  Newly registered users can never access the dashboard or banking portal.
* **Planned Fix:**
  * In `user-service`: Implement resilient fallback auto-creation in `getProfile` and `getMyProfile` so that if a profile does not exist yet, it is immediately initialized from header information (`X-User-Id`, `X-User-Email`, `X-User-Name`).
  * In `auth-service`: Directly initialize the user profile via `user-service`'s internal endpoint during registration.
  * In `api-gateway`: Forward `X-User-Name` header from the token's `fullName` claim so downstream services have the user's display name.

---

### 3. [HIGH] `admin-service` Feign Clients Missing Authentication Interceptor
* **Affected Files:**
  * `admin-service/src/main/java/com/banking/admin/config/FeignConfig.java` (New)
  * `admin-service/src/main/java/com/banking/admin/client/UserServiceClient.java`
  * `admin-service/src/main/java/com/banking/admin/client/AccountServiceClient.java`
  * `admin-service/src/main/java/com/banking/admin/client/TransactionServiceClient.java`
* **Root Cause:**
  `admin-service` calls `user-service`, `account-service`, and `transaction-service` using Spring Cloud OpenFeign. The target endpoints (such as `GET /api/v1/users`, `GET /api/v1/accounts/all`, `GET /api/v1/transactions/flagged`) are guarded by `@PreAuthorize("hasRole('ADMIN')")`. However, `admin-service` lacked a Feign `RequestInterceptor`, causing outbound HTTP requests to be sent without the `Authorization: Bearer <token>` header or `X-User-Role`. The receiving services reject the requests with HTTP 401 Unauthorized (`FeignException.Unauthorized`).
* **Impact:**
  The Admin Panel in the frontend cannot fetch users, accounts, or flagged transactions.
* **Planned Fix:**
  Add a Feign `RequestInterceptor` bean in `admin-service` that propagates the current request's `Authorization` bearer token and `X-User-*` headers to downstream microservices.

---

### 4. [HIGH] `notification-service` UserClient Fails with HTTP 401 Unauthorized
* **Affected Files:**
  * `notification-service/src/main/java/com/banking/notification/client/UserClient.java`
  * `notification-service/src/main/java/com/banking/notification/kafka/NotificationEventConsumer.java`
  * `user-service/src/main/java/com/banking/user/controller/UserController.java`
* **Root Cause:**
  `NotificationEventConsumer` processes background Kafka events (e.g. `user-events`, `transaction-events`) and invokes `userClient.getProfile(String.valueOf(userId))` to obtain recipient email addresses. `UserClient` called `GET /api/v1/users/me`, which is an authenticated user endpoint requiring a valid JWT session. Because the consumer runs as an asynchronous background task without user authentication, `user-service` rejects the call with HTTP 401.
* **Impact:**
  Welcome emails and fraud alert emails fail to send.
* **Planned Fix:**
  Add an internal endpoint `GET /api/v1/users/internal/id/{userId}` in `user-service` (configured as `permitAll()` under `/api/v1/users/internal/**`) and update `UserClient` in `notification-service` to call this internal endpoint.

---

### 5. [MEDIUM] Frontend Axios 401 Interceptor Breaks Login Error Notification
* **Affected Files:**
  * `frontend/src/api/axios.js`
* **Root Cause:**
  When a user enters incorrect credentials, the backend responds with HTTP 401 "Invalid email or password". The Axios response interceptor intercepted all 401 responses indiscriminately (including `/auth/login`), attempted to call the refresh endpoint with a missing/null `refreshToken`, failed, cleared `localStorage`, and executed `window.location.href = '/login'`. This reloaded the window before `Login.jsx`'s `catch` block could display the error toast. Additionally, the refresh URL was hardcoded to `http://localhost:8088/api/v1/auth/refresh`.
* **Impact:**
  Users entering wrong passwords experience a full page reload without any feedback message explaining why login failed.
* **Planned Fix:**
  Exclude `/auth/` routes from the 401 refresh interceptor, check if `refreshToken` exists before refreshing, use the configured dynamic `baseURL`, and properly set `Authorization` on retried requests.

---

### 6. [MEDIUM] Spring Cloud Gateway Predicate Syntax Whitespace Issue
* **Affected Files:**
  * `api-gateway/src/main/resources/application.yml`
* **Root Cause:**
  Routes `account-service` and `transaction-service` used comma-separated path patterns with a leading space:
  `- Path=/api/v1/accounts/**, /api/v1/beneficiaries/**`
  `- Path=/api/v1/transactions/**, /api/v1/statements/**`
  Depending on YAML and Gateway parser behavior, the second path pattern can retain the leading space (` /api/v1/beneficiaries/**`), preventing route matches for `/api/v1/beneficiaries` and `/api/v1/statements`.
* **Impact:**
  Requests to `/api/v1/beneficiaries` or statement downloads may fail to route to the correct service.
* **Planned Fix:**
  Remove extraneous spaces in comma-separated path predicates: `Path=/api/v1/accounts/**,/api/v1/beneficiaries/**`.

---

### 7. [MEDIUM] Enum String Literal Comparison in Hibernate 6 JPQL Queries
* **Affected Files:**
  * `transaction-service/src/main/java/com/banking/transaction/repository/TransactionRepository.java`
* **Root Cause:**
  `TransactionRepository` contains `@Query` annotations that compare enum fields to string literals:
  `t.transactionType = 'TRANSFER' AND t.status = 'SUCCESS'`.
  In Hibernate 6 (Spring Boot 3), strict type checking between Enum path expressions and String literals can cause `SemanticException` or query validation failures.
* **Impact:**
  Fraud detection queries (`findTransfersSince`, `sumTransferAmountSince`) can fail at runtime.
* **Planned Fix:**
  Use fully qualified enum constants in JPQL:
  `t.transactionType = com.banking.transaction.enums.TransactionType.TRANSFER AND t.status = com.banking.transaction.enums.TransactionStatus.SUCCESS`.

---

### 8. [LOW] Missing Loan Approval & Rejection Endpoints in `LoanController`
* **Affected Files:**
  * `loan-service/src/main/java/com/banking/loan/controller/LoanController.java`
* **Root Cause:**
  `LoanService` implemented `approveLoan(Long loanId)` and `rejectLoan(Long loanId, String reason)`, but `LoanController` never exposed HTTP endpoints for these actions.
* **Impact:**
  Admins could not approve or reject submitted loan applications via REST API.
* **Planned Fix:**
  Add `@PatchMapping("/{loanId}/approve")` and `@PatchMapping("/{loanId}/reject")` to `LoanController`.

---

### 9. [LOW] Frontend Notification Stream Hardcoded Host & Literal Newline
* **Affected Files:**
  * `frontend/src/components/layout/index.jsx`
* **Root Cause:**
  SSE connection URL in `Layout.jsx` was hardcoded to `http://localhost:8088/api/v1/notifications/stream` rather than deriving from `VITE_API_BASE_URL`. Also, line 63 used double-escaped `\\n` resulting in literal `\n` in toast messages.
* **Impact:**
  Breaks when deployed outside `localhost:8088`; formatting defect in live notification popups.
* **Planned Fix:**
  Use dynamic `import.meta.env.VITE_API_BASE_URL` and standard newline `\n`.

---

### 10. [LOW] Signup Form Discards PAN and Aadhar Numbers
* **Affected Files:**
  * `auth-service/src/main/java/com/banking/auth/dto/RegisterRequest.java`
* **Root Cause:**
  The frontend signup page provides optional PAN and Aadhar input fields, but `RegisterRequest` in `auth-service` omitted these fields, causing them to be ignored.
* **Impact:**
  Users who enter PAN and Aadhar during registration lose this data upon account creation.
* **Planned Fix:**
  Add `panNumber` and `aadharNumber` fields to `RegisterRequest` in `auth-service`.

---

### 11. [LOW] `NumberFormatException` Vulnerability on `X-User-Id` Header
* **Affected Files:**
  * `user-service/src/main/java/com/banking/user/controller/UserController.java`
  * `account-service/src/main/java/com/banking/account/controller/AccountController.java`
  * `loan-service/src/main/java/com/banking/loan/controller/LoanController.java`
  * `transaction-service/src/main/java/com/banking/transaction/controller/TransactionController.java`
  * `notification-service/src/main/java/com/banking/notification/controller/NotificationController.java`
* **Root Cause:**
  Controllers use `Long.parseLong(req.getHeader("X-User-Id"))` directly without checking if the header is present or empty. If a request is sent without this header, it results in an unhandled `NumberFormatException: null` (HTTP 500).
* **Impact:**
  Unhandled 500 error instead of a clean 400/401 response when headers are missing.
* **Planned Fix:**
  Add null/empty checks with informative bad request / unauthorized responses.
