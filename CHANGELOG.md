# Changelog — StayNear PG Rental

---

## Session: 2026-10-24

### Human Understanding
We found and fixed several serious security holes in the login and booking system. The app was generating weak, predictable OTP codes and was even returning the OTP directly in the API response — meaning anyone watching network traffic could steal it. We also found that two users could book the same last room at the same time, causing overbooking. The rate limiter wasn't working correctly behind a load balancer. And the app would crash without helpful error messages.

All of these are now fixed. The app is significantly more secure and more reliable than it was before this session.

---

### Technical Changes

#### Commit `be30e2c` — 2026-10-24
**OTP security hardening, proxy-aware rate limiting, remove devOtp leak**

| File | Change |
|------|--------|
| `AuthService.java` | Replaced `java.util.Random` with `SecureRandom`; upgraded OTP from 4-digit to 6-digit (1M space vs 10K); `requestOtp()` return type changed `String → void` so OTP never travels to caller |
| `AuthController.java` | Removed `devOtp` field from API response — OTP was being returned in plaintext to the client |
| `AuthService.java` | Replaced `"refresh-token-placeholder"` string with `null` |
| `RateLimitingInterceptor.java` | Added `resolveClientIp()` helper: reads `X-Forwarded-For` header first, falls back to `getRemoteAddr()`; without this, all requests behind a reverse proxy looked like the same IP |
| `AuthServiceTest.java` | Updated test to match void return type |

---

#### Commit `33356c5` — 2026-10-24
**Concurrency-safe room booking and README accuracy**

| File | Change |
|------|--------|
| `RoomInventoryRepository.java` | Added `findByPgIdAndTypeForUpdate()` with `@Lock(LockModeType.PESSIMISTIC_WRITE)` — holds a database row lock during the check-and-increment so two simultaneous requests cannot both see "1 room available" and both succeed |
| `BookingService.java` | `createBooking()` now calls the locking query instead of the plain read |
| `BookingServiceTest.java` | Updated mock calls to match new method name |
| `README.md` | Removed "N instances behind a load balancer", "millions of PGs", "production-first mindset" — none of these were backed by evidence |

---

#### Commit `2274fb2` — 2026-10-24
**OTP abuse protection and README repository structure**

| File | Change |
|------|--------|
| `AuthService.java` | Added 60-second cooldown per phone number after each OTP request (stored in Redis as `otp_cooldown:<phone>`) — prevents SMS flooding |
| `AuthService.java` | Added 5-attempt limit on OTP verification (Redis counter `otp_attempts:<phone>`, expires with the OTP TTL) — prevents brute-force guessing |
| `AuthServiceTest.java` | Added `requestOtp_withinCooldown_throws` and `verifyOtp_tooManyAttempts_throws` tests |
| `README.md` | Added Repository Structure table explaining `apps/backend-java/` (active Java backend) vs `backend/` (unused Node.js prototype) |

---

#### Commit `694fe31` — 2026-10-24
**JWT env secret, global error handler, input validation, expanded tests**

| File | Change |
|------|--------|
| `application.properties` | `jwt.secret` now reads `${JWT_SECRET}` with no default — app refuses to start if the variable is missing; previously had a hardcoded insecure default |
| `GlobalExceptionHandler.java` | New file — `@RestControllerAdvice` mapping `IllegalArgumentException → 400`, `RuntimeException → 400`, `HttpMessageNotReadableException → 400`, `Exception → 500`; all responses return `{"error": "..."}` JSON |
| `AuthController.java` | Added null/blank validation for `phone`, `role`, `otp` on every endpoint; invalid role string now returns 400 instead of 500 stack trace |
| `AuthServiceTest.java` | Added `verifyOtp_otpDeletedAfterUse_cannotReuse` and `verifyOtp_expiredOtp_throws` — 10 tests total |
| `BookingServiceTest.java` | Added `createBooking_fullRoom_throws` boundary test — 8 tests total |

---

### Required Environment Variables
These must be set before starting the backend:

```env
JWT_SECRET=<random 256-bit string>
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/pgrental
SPRING_DATASOURCE_USERNAME=<db user>
SPRING_DATASOURCE_PASSWORD=<db password>
RABBITMQ_HOST=localhost
REDIS_HOST=localhost
```

---

### Previous Commits (pre-session)

| Commit | Description |
|--------|-------------|
| `463a862` | Frontend wiring — TenantDashboard, OwnerDashboard, API URLs |
| `703cea9` | Swagger/OpenAPI annotations on all controllers |
| `e4a0fac` | Unit tests for AuthService and BookingService |
| `a4e5143` | Image upload for PG listings |
| `4a6b9ec` | Razorpay payment integration |
