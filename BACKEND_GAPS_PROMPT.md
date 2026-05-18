# Claude Code Prompt — Finalize Spring Boot Delivery System Backend

## Project Context

**Stack:** Spring Boot 3.4.5, Java 21, PostgreSQL, Redis, Gradle, Lombok, JWT (jjwt 0.12.x), Stripe, STOMP WebSocket, Spring Cache (Redis), Spring Security, OAuth2 (Google), Bucket4j rate limiting.

**Base package:** `com.delivery`  
**Auth:** JWT Bearer tokens. Users have roles: `ROLE_CUSTOMER`, `ROLE_DRIVER`, `ROLE_RESTAURANT_OWNER`, `ROLE_ADMIN`.

**What already exists and works:**

- JWT auth (register/login/refresh/logout via email+password), OAuth2 Google, rate limiting  
- `User` entity, `DriverProfile` entity (vehicleType, licensePlate, avgRating, isOnline, lastLat/Lng)  
- Restaurant CRUD \+ branch CRUD \+ name search \+ Redis caching  
- Full menu (categories → items → modifiers) CRUD keyed by `branchId`  
- Order lifecycle (place, cancel, status update, customer list, branch list) \+ WebSocket status notifications  
- Delivery tracking (driver location → Redis geo, markPickedUp/Delivered, online toggle)  
- Payment via Stripe (`/checkout` → PaymentIntent, `/webhook` stub)  
- Generic review system (`targetType` enum: RESTAURANT/DRIVER/DELIVERY, `targetId`)  
- Admin analytics summary (revenue, orders, new customers by date range)  
- Global exception handler, `PageResponse<T>` wrapper

**The Figma design** ([https://www.figma.com/design/qMAyadrZ3HIDQBbw6j0OeC/food-delivery-app](https://www.figma.com/design/qMAyadrZ3HIDQBbw6j0OeC/food-delivery-app)) is a **customer-facing Arabic food delivery app** with screens: Location picker, Home (banners \+ Most Ordered \+ Top Rated), Search \+ filters, Restaurant Detail (cover image, logo, name, rating+count, cuisine list, delivery time, delivery fee, offers/coupons, menu tabs), Product Detail (image, name, price, customisations), Cart, Orders list, Order tracking, Rate screen.

The **Flutter driver app** (separate project) expects a driver-specific API under `/api/v1/driver/...` for OTP auth, shift management, earnings, documents, and a driver-facing order workflow.

---

## PART 1 — Fix & Extend Existing Entities

### 1.1 Extend `Restaurant` entity

Add the following fields to `com.delivery.restaurant.domain.Restaurant`:

private String coverImageUrl;       // hero image shown in card & detail

private String logoImageUrl;        // circular logo shown in detail header

@Column(length \= 1000\)

private String description;

private Integer deliveryTimeMin;    // e.g. 30

private Integer deliveryTimeMax;    // e.g. 45

@Column(precision \= 10, scale \= 2\)

private BigDecimal deliveryFee;

@Column(precision \= 10, scale \= 2\)

private BigDecimal minimumOrder;

// cuisineType stays as a single string for now (comma-separated e.g. "شاورما, بيتزا")

// rating & ratingCount are computed from the review table — do NOT store them in the entity

### 1.2 Extend `RestaurantDto`

Add all new fields above to `RestaurantDto` \+ include computed fields:

private Double rating;          // avg from reviews

private Long ratingCount;       // count from reviews

private boolean isOpenNow;      // computed from branch operating hours

Update `RestaurantDto.from(Restaurant r)` to accept an optional `Double rating`, `Long ratingCount`, `boolean isOpenNow` via an overloaded factory or a builder call from `RestaurantService`.

### 1.3 Extend `PlaceOrderRequest`

Add to `com.delivery.order.dto.PlaceOrderRequest`:

@NotBlank  private String deliveryAddress;

@NotNull   private Double deliveryLat;

@NotNull   private Double deliveryLng;

           private String paymentMethod;     // "cash" | "card" | "wallet"

           private String promoCode;

           private String specialInstructions;

### 1.4 Extend `Order` entity

Add:

private String deliveryAddress;

private Double deliveryLat;

private Double deliveryLng;

private String paymentMethod;

private String promoCode;

@Column(precision \= 10, scale \= 2\)

private BigDecimal discount;     // promo discount applied

private String specialInstructions;

### 1.5 Extend `OrderDto`

Add:

private String restaurantName;

private String deliveryAddress;

private Double deliveryLat;

private Double deliveryLng;

private String paymentMethod;

private BigDecimal discount;

private Integer estimatedMinutes;  // populated after driver assignment

// Driver snapshot for customer tracking:

private String driverName;

private String driverPhone;

private Double driverLat;

private Double driverLng;

### 1.6 Extend `MenuItem` entity

Add:

@Column(precision \= 10, scale \= 2\)

private BigDecimal originalPrice;   // original price before discount (nullable)

### 1.7 Extend `MenuItemDto`

Add `originalPrice` field and include in `from()` factory.

---

## PART 2 — New Entities

Create all new JPA entities in the appropriate package. All use `@GeneratedValue(strategy = GenerationType.UUID)` and Lombok `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`.

### 2.1 `UserAddress` — `com.delivery.address.domain`

@Entity @Table(name \= "user\_addresses")

UUID id

UUID userId          // FK to users.id

String label         // "home" | "work" | "other"

String fullAddress

Double lat

Double lng

String city

String neighborhood

boolean isDefault

LocalDateTime createdAt

### 2.2 `Favorite` — `com.delivery.favorite.domain`

@Entity @Table(name \= "favorites",

    uniqueConstraints \= @UniqueConstraint(columnNames \= {"user\_id","restaurant\_id"}))

UUID id

UUID userId

UUID restaurantId

LocalDateTime createdAt  @PrePersist

### 2.3 `Banner` — `com.delivery.banner.domain`

@Entity @Table(name \= "banners")

UUID id

String imageUrl

String title

String subtitle

String ctaText

String discountText      // e.g. "خصم 50%"

String deepLink          // e.g. "/restaurants/{id}"

boolean isActive

int sortOrder

LocalDateTime createdAt

LocalDateTime expiresAt  // nullable

### 2.4 `Offer` (restaurant coupon) — `com.delivery.offer.domain`

@Entity @Table(name \= "offers")

UUID id

UUID restaurantId

String title

int discountPercent

BigDecimal minOrderAmount

String description

boolean isActive

LocalDateTime expiresAt

LocalDateTime createdAt  @PrePersist

### 2.5 `Cart` — `com.delivery.cart.domain`

@Entity @Table(name \= "carts")

UUID id

UUID userId              // one cart per user

UUID restaurantId

UUID branchId

@OneToMany(mappedBy \= "cart", cascade \= CascadeType.ALL, orphanRemoval \= true)

List\<CartItem\> items

LocalDateTime updatedAt  @PreUpdate @PrePersist

### 2.6 `CartItem` — `com.delivery.cart.domain`

@Entity @Table(name \= "cart\_items")

UUID id

@ManyToOne(fetch \= LAZY) @JoinColumn(name \= "cart\_id") Cart cart

UUID menuItemId

String itemName

int quantity

BigDecimal unitPrice

@JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition \= "jsonb")

List\<Map\<String, Object\>\> selectedModifiers

### 2.7 `DriverDocument` — `com.delivery.driver.domain`

@Entity @Table(name \= "driver\_documents")

UUID id

UUID driverId            // FK to driver\_profiles.id

String type              // "national\_id" | "driving\_license" | "vehicle\_registration" | "vehicle\_photo"

String fileUrl

String status            // "pending" | "approved" | "rejected"

LocalDateTime uploadedAt @PrePersist

### 2.8 `Shift` — `com.delivery.driver.domain`

@Entity @Table(name \= "shifts")

UUID id

LocalDate date

LocalTime startTime

LocalTime endTime

int maxDrivers

@Builder.Default boolean isActive \= true

LocalDateTime createdAt @PrePersist

### 2.9 `ShiftBooking` — `com.delivery.driver.domain`

@Entity @Table(name \= "shift\_bookings",

    uniqueConstraints \= @UniqueConstraint(columnNames \= {"driver\_id","shift\_id"}))

UUID id

UUID driverId

UUID shiftId

LocalDateTime bookedAt @PrePersist

### 2.10 `DriverEarnings` — `com.delivery.driver.domain`

@Entity @Table(name \= "driver\_earnings")

UUID id

UUID driverId

UUID orderId             // nullable (for bonus/incentive records)

String type              // "trip" | "tip" | "bonus" | "withdrawal"

BigDecimal amount

String description

LocalDateTime createdAt @PrePersist

### 2.11 `Incentive` — `com.delivery.driver.domain`

@Entity @Table(name \= "incentives")

UUID id

String title

String description

int targetTrips

BigDecimal bonusAmount

LocalDateTime deadline

boolean isActive

LocalDateTime createdAt @PrePersist

### 2.12 `FcmToken` — `com.delivery.notification.domain`

@Entity @Table(name \= "fcm\_tokens",

    uniqueConstraints \= @UniqueConstraint(columnNames \= {"user\_id","token"}))

UUID id

UUID userId

String token

String platform         // "ios" | "android"

LocalDateTime updatedAt @PrePersist @PreUpdate

### 2.13 `OtpRecord` — `com.delivery.auth.domain`

@Entity @Table(name \= "otp\_records")

UUID id

String phone            // E.164 format

String code             // 6-digit hashed or plain

LocalDateTime expiresAt

boolean used

LocalDateTime createdAt @PrePersist

---

## PART 3 — New Repositories

Create Spring Data JPA repositories for every new entity above. Include all custom query methods needed by the services below.

### Notable custom queries needed:

**`RestaurantRepository`** — add:

// For home discovery

@Query("SELECT r FROM Restaurant r WHERE r.isActive \= true ORDER BY ...")

Page\<Restaurant\> findMostOrdered(Pageable pageable); // join with order count

@Query("SELECT r FROM Restaurant r WHERE r.isActive \= true " \+

       "AND (:cuisineType IS NULL OR LOWER(r.cuisineType) LIKE LOWER(CONCAT('%',:cuisineType,'%'))) " \+

       "AND (:minRating IS NULL OR \<avg rating subquery\> \>= :minRating) " \+

       "AND (:maxDeliveryTime IS NULL OR r.deliveryTimeMax \<= :maxDeliveryTime) " \+

       "ORDER BY r.name")

Page\<Restaurant\> filterActive(String cuisineType, Double minRating,

                               Integer maxDeliveryTime, Pageable pageable);

**`ReviewRepository`** — add:

@Query("SELECT AVG(r.rating) FROM Review r WHERE r.targetType \= :t AND r.targetId \= :id")

Double avgRatingByTarget(TargetType t, UUID id);

@Query("SELECT COUNT(r) FROM Review r WHERE r.targetType \= :t AND r.targetId \= :id")

Long countByTarget(TargetType t, UUID id);

boolean existsByReviewerIdAndTargetTypeAndTargetId(UUID reviewerId, TargetType t, UUID id);

**`OrderRepository`** — add:

Page\<Order\> findAllByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

Page\<Order\> findAllByBranchIdOrderByCreatedAtDesc(UUID branchId, Pageable pageable);

long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

// For driver earnings analytics:

@Query("SELECT COUNT(d) FROM Delivery d WHERE d.driverId \= :driverId " \+

       "AND d.status \= 'DELIVERED' AND d.deliveredTime BETWEEN :from AND :to")

long countDeliveredByDriverInRange(UUID driverId, LocalDateTime from, LocalDateTime to);

**`TransactionRepository`** — add:

Optional\<Transaction\> findByStripePaymentIntentId(String id);

@Query("SELECT COALESCE(SUM(t.amount), 0\) FROM Transaction t " \+

       "WHERE t.status \= 'SUCCEEDED' AND t.createdAt BETWEEN :from AND :to")

BigDecimal sumRevenueByDateRange(LocalDateTime from, LocalDateTime to);

**`DriverEarningsRepository`** — add:

@Query("SELECT COALESCE(SUM(e.amount), 0\) FROM DriverEarnings e " \+

       "WHERE e.driverId \= :driverId AND e.type \<\> 'withdrawal' " \+

       "AND e.createdAt BETWEEN :from AND :to")

BigDecimal sumByDriverAndDateRange(UUID driverId, LocalDateTime from, LocalDateTime to);

List\<DriverEarnings\> findByDriverIdOrderByCreatedAtDesc(UUID driverId, Pageable pageable);

**`DeliveryRepository`** — add:

Optional\<Delivery\> findByOrderId(UUID orderId);

List\<Delivery\> findByDriverIdAndStatus(UUID driverId, DeliveryStatus status);

**`FavoriteRepository`**:

boolean existsByUserIdAndRestaurantId(UUID userId, UUID restaurantId);

Optional\<Favorite\> findByUserIdAndRestaurantId(UUID userId, UUID restaurantId);

List\<Favorite\> findAllByUserId(UUID userId);

**`CartRepository`**:

Optional\<Cart\> findByUserId(UUID userId);

**`OfferRepository`**:

List\<Offer\> findAllByRestaurantIdAndIsActiveTrue(UUID restaurantId);

**`BannerRepository`**:

List\<Banner\> findAllByIsActiveTrueOrderBySortOrderAsc();

**`OtpRecordRepository`**:

Optional\<OtpRecord\> findTopByPhoneAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(

    String phone, LocalDateTime now);

**`FcmTokenRepository`**:

List\<FcmToken\> findAllByUserId(UUID userId);

void deleteByUserIdAndToken(UUID userId, String token);

**`ShiftRepository`**:

List\<Shift\> findAllByIsActiveTrueAndDateGreaterThanEqualOrderByDateAsc(LocalDate from);

---

## PART 4 — OTP Authentication

Add phone-number-based OTP auth **in addition to** the existing email/password auth.

### 4.1 New DTOs — `com.delivery.auth.dto`

// SendOtpRequest.java

@NotBlank @Pattern(regexp="\\\\+?\[0-9\]{10,15}") String phone;

@NotBlank @Pattern(regexp="ROLE\_CUSTOMER|ROLE\_DRIVER") String role;

// VerifyOtpRequest.java

@NotBlank String phone;

@NotBlank @Size(min=6, max=6) String otp;

### 4.2 `OtpService` — `com.delivery.auth.service`

@Service

public class OtpService {

    // Inject OtpRecordRepository, UserRepository, PasswordEncoder (for hashing otp)

    public void sendOtp(String phone, String role) {

        // 1\. Generate 6-digit random code

        // 2\. Hash it (BCrypt or store plain in dev — make configurable via app.otp.hash-codes)

        // 3\. Save OtpRecord(phone, hashedCode, expiresAt \= now+5min, used=false)

        // 4\. Log the OTP (replace with Twilio/SMS gateway call in prod):

        //    log.info("OTP for {}: {}", phone, code)

        // 5\. If user doesn't exist, create User(phone=phone, role=Role.valueOf(role), active=false)

    }

    public AuthResponse verifyOtp(String phone, String otp) {

        // 1\. Find latest valid OtpRecord for phone

        // 2\. Verify code (BCrypt.matches or plain equals)

        // 3\. Mark OtpRecord.used \= true

        // 4\. Activate user (user.setActive(true))

        // 5\. Return buildAuthResponse(user) — same as AuthService

    }

}

### 4.3 Extend `AuthController`

Add two new endpoints:

@PostMapping("/otp/send")

public ResponseEntity\<Void\> sendOtp(@Valid @RequestBody SendOtpRequest req)

@PostMapping("/otp/verify")

public ResponseEntity\<AuthResponse\> verifyOtp(@Valid @RequestBody VerifyOtpRequest req)

Add both to `permitAll()` in `SecurityConfig`.

---

## PART 5 — Customer-facing APIs

### 5.1 User Profile Controller — `com.delivery.user.controller.UserController`

`GET /api/v1/user/profile` — `@PreAuthorize("hasRole('CUSTOMER')")` — return `UserProfileDto` (id, name, phone, email, avatar)

`PUT /api/v1/user/profile` — update name, phone, email, avatar URL

Create `UserProfileDto` with those fields \+ `UpdateUserProfileRequest`.

### 5.2 Address Controller — `com.delivery.address.controller.AddressController`

All endpoints `@PreAuthorize("hasRole('CUSTOMER')")`:

GET    /api/v1/user/addresses        → List\<AddressDto\>

POST   /api/v1/user/addresses        → AddressDto (201)

PUT    /api/v1/user/addresses/{id}   → AddressDto

DELETE /api/v1/user/addresses/{id}   → 204

PATCH  /api/v1/user/addresses/{id}/default → AddressDto

`AddressDto` fields: `id`, `label`, `fullAddress`, `lat`, `lng`, `city`, `neighborhood`, `isDefault`.

`AddressService`: standard CRUD, on `setDefault` flip all others `isDefault=false` for that user.

### 5.3 Banner Controller — `com.delivery.banner.controller.BannerController`

GET  /api/v1/banners                              → List\<BannerDto\>  (public, no auth)

POST /api/v1/admin/banners   @hasRole('ADMIN')    → BannerDto (201)

`BannerDto`: all Banner fields. `BannerService.getActiveBanners()` — cached (`@Cacheable("banners")`).

### 5.4 Extend Restaurant Controller

Add to `RestaurantController`:

// Home discovery

GET /api/v1/restaurants/most-ordered

    → returns top-10 restaurants ordered by order count (last 30 days)

    → PageResponse\<RestaurantDto\> with rating \+ ratingCount populated

GET /api/v1/restaurants/top-rated

    → returns top-10 restaurants ordered by avg review rating DESC

// Filtering (extend existing GET /api/v1/restaurants)

// Add query params: cuisineType, minRating, maxDeliveryTime, isAvailable

// Delegate to RestaurantService.filterActive(...)

// Offers

GET /api/v1/restaurants/{id}/offers  → List\<OfferDto\>  (public)

POST /api/v1/restaurants/{id}/offers @hasAnyRole('RESTAURANT\_OWNER','ADMIN') → OfferDto (201)

// Favorites

POST   /api/v1/restaurants/{id}/favorite  @hasRole('CUSTOMER') → { isFavorite: true }

DELETE /api/v1/restaurants/{id}/favorite  @hasRole('CUSTOMER') → 204

GET    /api/v1/restaurants/favorites      @hasRole('CUSTOMER') → List\<RestaurantDto\>

Create `OfferDto` (id, restaurantId, title, discountPercent, minOrderAmount, description, expiresAt) and `OfferService`.

For favorites, create `FavoriteService` that toggles the `Favorite` entity and returns current state.

When building `RestaurantDto`, join the review table to populate `rating` and `ratingCount`. Add a helper in `RestaurantService`:

private RestaurantDto enrich(Restaurant r) {

    Double rating \= reviewRepository.avgRatingByTarget(TargetType.RESTAURANT, r.getId());

    Long count \= reviewRepository.countByTarget(TargetType.RESTAURANT, r.getId());

    return RestaurantDto.from(r, rating \!= null ? rating : 0.0, count \!= null ? count : 0L, isOpenNow(r));

}

### 5.5 Extend Menu Controller

Add to `MenuController`:

// By restaurant ID (customer-friendly — picks the single active branch)

GET /api/v1/restaurants/{restaurantId}/menu → List\<MenuCategoryDto\>

In `MenuService.getMenuByRestaurant(UUID restaurantId)`:

1. Find the first active branch for the restaurant via `BranchRepository.findFirstByRestaurantIdAndIsActiveTrue(restaurantId)`  
2. Delegate to existing `getFullMenu(branchId)`

### 5.6 Cart Controller — `com.delivery.cart.controller.CartController`

All endpoints `@PreAuthorize("hasRole('CUSTOMER')")`:

GET    /api/v1/user/cart                   → CartDto

POST   /api/v1/user/cart/items             → CartDto  (add item)

PUT    /api/v1/user/cart/items/{itemId}    → CartDto  (update quantity)

DELETE /api/v1/user/cart/items/{itemId}    → CartDto  (remove item)

DELETE /api/v1/user/cart                   → 204      (clear cart)

`CartDto`: `id`, `restaurantId`, `restaurantName`, `items[]` (CartItemDto: itemId, itemName, quantity, unitPrice, totalPrice, selectedModifiers), `subtotal`, `deliveryFee`, `discount`, `total`.

`CartService`:

- `getOrCreate(userId)` — find or create cart  
- `addItem(userId, AddCartItemRequest)` — if different restaurant, throw `"Clear cart to order from a different restaurant"` (409 Conflict)  
- `updateItem(userId, cartItemId, quantity)` — set quantity; if 0 remove item  
- `removeItem(userId, cartItemId)`  
- `clearCart(userId)`

`AddCartItemRequest` DTO: `menuItemId`, `quantity`, `selectedModifiers`.

### 5.7 Extend Order Controller & Service

**In `OrderService.placeOrder`:**

- Map new fields from `PlaceOrderRequest` (deliveryAddress, deliveryLat/Lng, paymentMethod, promoCode, specialInstructions) onto the `Order` entity  
- If `promoCode` is provided, validate against a `PromoCode` table (optional — stub with TODO if table not yet implemented)  
- After saving, clear the user's cart: `cartRepository.findByUserId(customerId).ifPresent(cartRepository::delete)`

**Add to `OrderController`:**

GET /api/v1/orders/{id}/tracking → OrderTrackingDto

`OrderTrackingDto`: `orderId`, `status`, `estimatedMinutes`, `driverName`, `driverPhone`, `driverLat`, `driverLng`, `timeline` (List of {status, timestamp}).

`OrderService.getTracking(UUID orderId)`:

1. Load order  
2. Load delivery via `deliveryRepository.findByOrderId(orderId)` — get driver lat/lng  
3. Load driver name/phone via `userRepository.findById(delivery.getDriverId())`  
4. Return assembled DTO

### 5.8 Review Endpoint for Order Rating

Add to `ReviewController`:

// Submit combined restaurant \+ driver rating in one call

POST /api/v1/orders/{orderId}/rating

@PreAuthorize("hasRole('CUSTOMER')")

Body: { restaurantRating: 1-5, driverRating: 1-5, comment: String? }

`OrderRatingRequest` DTO: `restaurantRating` (1-5), `driverRating` (1-5, nullable), `comment`.

In `ReviewService.submitOrderRating(UUID customerId, UUID orderId, OrderRatingRequest req)`:

1. Load order, verify `order.getCustomerId().equals(customerId)` and `order.getStatus() == DELIVERED`  
2. Load delivery to get `driverId` and `restaurantId` from branch  
3. Create `Review(RESTAURANT, branchId, restaurantRating)` if not already exists  
4. Create `Review(DRIVER, driverId, driverRating)` if not already exists and driverRating provided  
5. After creating restaurant review, update `DriverProfile.avgRating` via running average

---

## PART 6 — Driver-facing APIs

Create a new controller `com.delivery.driver.controller.DriverApiController` mapped to `/api/v1/driver`. All endpoints require `@PreAuthorize("hasRole('DRIVER')")` unless noted.

### 6.1 Driver Registration / Onboarding

POST /api/v1/driver/register   (no auth — called after OTP verify with ROLE\_DRIVER token)

Body: { vehicleType, vehicleNumber (licensePlate), nationalId }

Action: create/update DriverProfile for the authenticated user

Add `nationalId` field to `DriverProfile` entity:

private String nationalId;

private String profilePhotoUrl;

@Enumerated(EnumType.STRING) @Builder.Default

private DriverStatus status \= DriverStatus.PENDING;  // PENDING | APPROVED | REJECTED

Create `DriverStatus` enum in `com.delivery.user.domain`.

GET /api/v1/driver/profile    → DriverProfileDto

PUT /api/v1/driver/profile    → DriverProfileDto

Body: { vehicleType, vehicleNumber, profilePhotoUrl }

`DriverProfileDto`: id, userId, name, phone, vehicleType, licensePlate, nationalId, profilePhotoUrl, avgRating, status, isOnline.

### 6.2 Driver Status

POST /api/v1/driver/status

Body: { is\_online: boolean }

Action: delegates to DeliveryService.setDriverOnline(principalId, isOnline)

Response: 200 { is\_online: true }

### 6.3 Driver Today Stats

GET /api/v1/driver/stats/today → DriverStatsDto

`DriverStatsDto`: `totalTripsToday`, `totalEarningsToday`, `hoursOnline` (stub 0.0 — needs shift data), `acceptanceRate` (stub 100.0 until decline tracking is added), `completionRate`.

`DriverStatsService.getTodayStats(UUID driverId)`:

1. `LocalDateTime start = LocalDate.now().atStartOfDay()`  
2. `totalTrips = orderRepository.countDeliveredByDriverInRange(driverId, start, now)`  
3. `totalEarnings = driverEarningsRepository.sumByDriverAndDateRange(driverId, start, now)`  
4. Return DTO

### 6.4 Shift Management

GET  /api/v1/driver/shifts           → List\<ShiftDto\>

POST /api/v1/driver/shifts/{shiftId}/book → ShiftBookingDto (201)

`ShiftDto`: id, date, startTime, endTime, maxDrivers, bookedCount, isBookedByMe.

`ShiftService`:

- `getAvailableShifts(UUID driverId)` — future shifts that aren't full  
- `bookShift(UUID driverId, UUID shiftId)` — check capacity, save `ShiftBooking`

Admin endpoints:

POST /api/v1/admin/shifts   @hasRole('ADMIN') → ShiftDto (201)

### 6.5 Driver Order Workflow

GET  /api/v1/driver/orders/active          → OrderDto (the currently assigned delivery)

POST /api/v1/driver/orders/{id}/accept     → OrderDto

POST /api/v1/driver/orders/{id}/decline    → 204

POST /api/v1/driver/orders/{id}/arrived    → 204 (driver arrived at restaurant)

POST /api/v1/driver/orders/{id}/confirm    → 204 (multipart: photo file)

POST /api/v1/driver/orders/{id}/issue      → 204

Body for issue: { type: String, description: String }

In `DriverOrderService` (new service):

- `getActiveOrder(UUID driverId)` — find Delivery where `driverId = X AND status IN (ASSIGNED, EN_ROUTE_TO_RESTAURANT, PICKED_UP, EN_ROUTE_TO_CUSTOMER)` → load Order and enrich with restaurant name  
- `acceptOrder(UUID driverId, UUID orderId)` — validate delivery belongs to driver, set status `EN_ROUTE_TO_RESTAURANT`, push WebSocket event to `/topic/orders/{orderId}/status`  
- `declineOrder(UUID driverId, UUID orderId)` — remove driver assignment, reset `Delivery.driverId = null`, re-queue order for assignment (fire a Spring `ApplicationEvent`)  
- `arrivedAtRestaurant(UUID driverId, UUID orderId)` — set `Delivery.status = EN_ROUTE_TO_RESTAURANT` → `OrderStatus.READY_FOR_PICKUP`  
- `confirmDelivery(UUID driverId, UUID orderId, MultipartFile photo)` — save photo to file storage (stub: save to local `/uploads/{orderId}-proof.jpg`), delegate to `DeliveryService.markDelivered`, create a `DriverEarnings(type=trip, amount=order.deliveryFee)`  
- `reportIssue(UUID driverId, UUID orderId, Map body)` — set order status `CANCELLED`, save an issue note (stub: log.warn)

### 6.6 Driver Location (Alternative URL)

POST /api/v1/driver/location

Body: { lat: double, lng: double }

Action: find active delivery for driver → delegate to DeliveryService.updateLocation

Also push WebSocket event to /topic/orders/{orderId}/driver-location

Add to `OrderStatusNotificationService`:

public void notifyDriverLocation(UUID orderId, double lat, double lng) {

    messagingTemplate.convertAndSend(

        "/topic/orders/" \+ orderId \+ "/driver-location",

        Map.of("orderId", orderId, "lat", lat, "lng", lng)

    );

}

### 6.7 Earnings

GET /api/v1/driver/earnings?period=today|week|month   → EarningsSummaryDto

GET /api/v1/driver/earnings/history?from=\&to=          → Page\<EarningsTripDto\>

GET /api/v1/driver/incentives                          → List\<IncentiveDto\>

GET /api/v1/driver/performance                         → PerformanceDto

POST /api/v1/driver/earnings/withdraw                  → 204 (stub — log withdrawal request)

**`EarningsSummaryDto`**: `totalEarnings`, `periodLabel`, `tripsAmount`, `tipsAmount`, `bonusAmount`, `withdrawableBalance`, `pendingBalance`.

`EarningsService.getSummary(UUID driverId, String period)`:

- Compute `from/to` dates based on period  
- Query `DriverEarningsRepository` for sums by type  
- `withdrawableBalance = totalEarnings - sum(withdrawals)`

**`EarningsTripDto`**: `id`, `date`, `orderId`, `amount`, `type`, `description`.

**`IncentiveDto`**: all Incentive fields \+ `completedTrips` (count from DriverEarnings/Delivery for this driver) \+ `isCompleted`.

**`PerformanceDto`**: `acceptanceRate` (stub 100.0), `completionRate` (delivered / accepted), `averageRating` (from reviews), `onTimeRate` (stub 95.0), `weeklyTrend` (List— last 7 days trip count / max).

### 6.8 Driver Documents

GET  /api/v1/driver/documents                    → List\<DriverDocumentDto\>

POST /api/v1/driver/documents (multipart)        → DriverDocumentDto (201)

     fields: type (form param), file (MultipartFile)

`DriverDocumentService`:

- `upload(UUID driverId, String type, MultipartFile file)` — save file to `uploads/documents/{driverId}/{type}-{uuid}.ext`, save `DriverDocument(driverId, type, fileUrl, status=PENDING)`  
- `getDocuments(UUID driverId)` — list all

Admin endpoint:

PATCH /api/v1/admin/documents/{id}/status?status=APPROVED|REJECTED  @hasRole('ADMIN')

### 6.9 Surge Zones

GET /api/v1/driver/zones → List\<ZoneDto\>

**Stub implementation** — no DB needed yet. Return a hardcoded list of 3 demo surge zones:

// In ZoneService.getDemoZones():

return List.of(

  new ZoneDto(UUID.randomUUID(), "Zone A", 1.5, "\#FF5733",

      List.of(Map.of("lat", 30.0, "lng", 31.0), /\* ... \*/)),

  ...

);

`ZoneDto`: `id`, `label`, `multiplier`, `color`, `polygon` (List\<Map\<lat,lng\>\>), `centerLat`, `centerLng`.

### 6.10 Driver Payout Details

GET /api/v1/driver/payout   → PayoutDetailsDto

PUT /api/v1/driver/payout   → PayoutDetailsDto

Body: { bankName, accountNumber, accountHolder }

Add a new `DriverPayout` entity or embed these fields in `DriverProfile`:

// Add to DriverProfile:

private String bankName;

private String bankAccountNumber;

private String bankAccountHolder;

---

## PART 7 — FCM Push Notifications

### 7.1 FCM Token Endpoint

POST /api/v1/notifications/token   (authenticated — any role)

Body: { token: String, platform: "ios" | "android" }

Action: upsert FcmToken for current user

DELETE /api/v1/notifications/token

Body: { token: String }

### 7.2 `FcmNotificationService` — `com.delivery.notification.service`

@Service

public class FcmNotificationService {

    // Inject FcmTokenRepository

    // Stub — log notifications; replace with Firebase Admin SDK call in prod

    public void sendToUser(UUID userId, String title, String body, Map\<String, String\> data) {

        List\<FcmToken\> tokens \= fcmTokenRepository.findAllByUserId(userId);

        tokens.forEach(t \-\> log.info("\[FCM\] → {} | {} | {} | {}", t.getToken(), title, body, data));

        // TODO: replace with FirebaseMessaging.getInstance().sendEachForMulticast(...)

    }

}

**Inject `FcmNotificationService` into `OrderStatusNotificationService`** and call it whenever order status changes:

- Status → CONFIRMED: notify customer "Your order has been confirmed"  
- Status → OUT\_FOR\_DELIVERY (= PICKED\_UP): notify customer "Driver is on the way"  
- Status → DELIVERED: notify customer "Order delivered\! Rate your experience"  
- New order assigned: notify driver "New order available"

---

## PART 8 — Fix Stripe Webhook

Replace the stub `PaymentController.webhook()` with a real implementation:

@PostMapping("/webhook")

public ResponseEntity\<Void\> webhook(

        @RequestBody String payload,

        @RequestHeader("Stripe-Signature") String sigHeader) {

    try {

        Event event \= Webhook.constructEvent(payload, sigHeader,

                stripeWebhookSecret);  // inject @Value("${app.stripe.webhook-secret}")

        if ("payment\_intent.succeeded".equals(event.getType())) {

            PaymentIntent intent \= (PaymentIntent) event.getDataObjectDeserializer()

                    .getObject().orElseThrow();

            paymentService.handleWebhookSuccess(intent.getId());

        }

        return ResponseEntity.ok().build();

    } catch (SignatureVerificationException e) {

        return ResponseEntity.status(HttpStatus.BAD\_REQUEST).build();

    }

}

---

## PART 9 — Fix Missing Repository Methods & GlobalExceptionHandler

### 9.1 Add `ResourceNotFoundException` — `com.delivery.common.exception`

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) { super(message); }

}

Replace all `throw new RuntimeException("X not found")` across services with `throw new ResourceNotFoundException("X not found")`.

Add handler in `GlobalExceptionHandler`:

@ExceptionHandler(ResourceNotFoundException.class)

ProblemDetail handleNotFound(ResourceNotFoundException ex) {

    return problem(HttpStatus.NOT\_FOUND, ex.getMessage());

}

### 9.2 Add `ConflictException` — `com.delivery.common.exception`

For cases like "cart has items from different restaurant", "already reviewed":

public class ConflictException extends RuntimeException { ... }

Add `@ExceptionHandler(ConflictException.class)` → `HttpStatus.CONFLICT`.

---

## PART 10 — Security Config Updates

Add the following to the `permitAll()` list in `SecurityConfig`:

"/api/v1/auth/otp/send",

"/api/v1/auth/otp/verify",

"/api/v1/restaurants",

"/api/v1/restaurants/\*\*",

"/api/v1/banners",

"/api/v1/menus/\*\*"

Confirm all existing auth endpoints remain permitted.

---

## PART 11 — application.yml additions

Add the following properties:

app:

  otp:

    expiry-minutes: 5

    hash-codes: false          \# set true in prod to BCrypt-hash the OTP

  uploads:

    base-path: ./uploads       \# local file storage base path

  driver:

    default-search-radius-km: 5.0

  fcm:

    enabled: false             \# set true once Firebase Admin SDK is wired

---

## Summary of Files to Create / Modify

| Category | Action | Count |
| :---- | :---- | :---- |
| Existing entities to extend (Restaurant, Order, MenuItem, DriverProfile) | Modify | 4 |
| Existing DTOs to extend (RestaurantDto, OrderDto, PlaceOrderRequest, MenuItemDto) | Modify | 4 |
| New entities (UserAddress, Favorite, Banner, Offer, Cart, CartItem, DriverDocument, Shift, ShiftBooking, DriverEarnings, Incentive, FcmToken, OtpRecord) | Create | 13 |
| New repositories for above \+ query additions to existing ones | Create/Modify | 15 |
| New services (OtpService, AddressService, BannerService, OfferService, FavoriteService, CartService, DriverStatsService, ShiftService, EarningsService, DriverDocumentService, FcmNotificationService, DriverOrderService, ZoneService) | Create | 13 |
| New controllers (UserController, AddressController, BannerController, DriverApiController, FcmTokenController) | Create | 5 |
| New DTOs (SendOtpRequest, VerifyOtpRequest, UserProfileDto, AddressDto, BannerDto, OfferDto, CartDto, CartItemDto, AddCartItemRequest, EarningsSummaryDto, EarningsTripDto, IncentiveDto, PerformanceDto, DriverStatsDto, DriverProfileDto, ShiftDto, ZoneDto, PayoutDetailsDto, OrderRatingRequest, OrderTrackingDto) | Create | 20 |
| Fixes (webhook, exceptions, SecurityConfig, application.yml) | Modify | 4 |
| **Total** |  | **\~78 files** |

---

## Implementation Order (recommended)

1. **Entities \+ Migrations** — extend existing, add new ones (DDL runs via `ddl-auto: update`)  
2. **Repositories** — add all custom query methods  
3. **Exceptions** — add `ResourceNotFoundException`, `ConflictException`, update handler  
4. **OTP Auth** — OtpRecord, OtpService, extend AuthController, update SecurityConfig  
5. **Customer APIs** — in order: User Profile → Addresses → Banners → Restaurant enrichment → Favorites → Offers → Menu-by-restaurant → Cart → Order extensions → Order Tracking → Order Rating  
6. **Driver APIs** — DriverApiController → Driver Profile → Status → Stats → Shifts → Order workflow → Location → Earnings → Documents → Zones → Payout  
7. **FCM** — FcmToken, FcmNotificationService, wire into OrderStatusNotificationService  
8. **Stripe webhook** — replace stub  
9. **Tests** — add `@SpringBootTest` integration tests for at minimum: OTP flow, place+track order flow, driver accept+deliver flow

