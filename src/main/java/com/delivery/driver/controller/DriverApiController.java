package com.delivery.driver.controller;

import com.delivery.common.security.UserPrincipal;
import com.delivery.delivery.service.DeliveryService;
import com.delivery.driver.domain.Shift;
import com.delivery.driver.dto.*;
import com.delivery.driver.service.*;
import com.delivery.order.dto.OrderDto;
import com.delivery.order.service.OrderStatusNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/driver")
@RequiredArgsConstructor
public class DriverApiController {

    private final DriverProfileService driverProfileService;
    private final DriverStatsService driverStatsService;
    private final ShiftService shiftService;
    private final DriverOrderService driverOrderService;
    private final EarningsService earningsService;
    private final DriverDocumentService driverDocumentService;
    private final ZoneService zoneService;
    private final DeliveryService deliveryService;
    private final OrderStatusNotificationService notificationService;

    // ── 6.1 Registration / Onboarding ──────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<DriverProfileDto> register(
            @RequestBody DriverRegisterRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(driverProfileService.register(principal.getId(), req));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverProfileDto> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(driverProfileService.getProfile(principal.getId()));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverProfileDto> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateDriverProfileRequest req) {
        return ResponseEntity.ok(driverProfileService.updateProfile(principal.getId(), req));
    }

    // ── 6.2 Driver Status ──────────────────────────────────────────────────

    @PostMapping("/status")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Map<String, Boolean>> setStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Boolean> body) {
        boolean isOnline = Boolean.TRUE.equals(body.get("is_online"));
        deliveryService.setDriverOnline(principal.getId(), isOnline);
        return ResponseEntity.ok(Map.of("is_online", isOnline));
    }

    // ── 6.3 Today Stats ───────────────────────────────────────────────────

    @GetMapping("/stats/today")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverStatsDto> todayStats(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(driverStatsService.getTodayStats(principal.getId()));
    }

    // ── 6.4 Shift Management ──────────────────────────────────────────────

    @GetMapping("/shifts")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<ShiftDto>> getShifts(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(shiftService.getAvailableShifts(principal.getId()));
    }

    @PostMapping("/shifts/{shiftId}/book")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ShiftBookingDto> bookShift(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID shiftId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shiftService.bookShift(principal.getId(), shiftId));
    }

    // ── 6.5 Driver Order Workflow ─────────────────────────────────────────

    @GetMapping("/orders/active")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<OrderDto> getActiveOrder(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(driverOrderService.getActiveOrder(principal.getId()));
    }

    @PostMapping("/orders/{id}/accept")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<OrderDto> acceptOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(driverOrderService.acceptOrder(principal.getId(), id));
    }

    @PostMapping("/orders/{id}/decline")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Void> declineOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        driverOrderService.declineOrder(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/orders/{id}/arrived")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Void> arrived(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        driverOrderService.arrivedAtRestaurant(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/orders/{id}/confirm")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Void> confirmDelivery(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestParam(required = false) MultipartFile photo) {
        driverOrderService.confirmDelivery(principal.getId(), id, photo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/orders/{id}/issue")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Void> reportIssue(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        driverOrderService.reportIssue(principal.getId(), id, body);
        return ResponseEntity.noContent().build();
    }

    // ── 6.6 Driver Location ───────────────────────────────────────────────

    @PostMapping("/location")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Void> updateLocation(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Double> body) {
        double lat = body.get("lat");
        double lng = body.get("lng");

        deliveryService.findActiveDeliveryForDriver(principal.getId()).ifPresent(delivery -> {
            var req = new com.delivery.delivery.dto.LocationUpdateRequest();
            req.setLat(lat);
            req.setLng(lng);
            deliveryService.updateLocation(delivery.getId(), principal.getId(), req);
            notificationService.notifyDriverLocation(delivery.getOrderId(), lat, lng);
        });

        return ResponseEntity.ok().build();
    }

    // ── 6.7 Earnings ─────────────────────────────────────────────────────

    @GetMapping("/earnings")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<EarningsSummaryDto> earningsSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false, defaultValue = "today") String period) {
        return ResponseEntity.ok(earningsService.getSummary(principal.getId(), period));
    }

    @GetMapping("/earnings/history")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<EarningsTripDto>> earningsHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(earningsService.getHistory(principal.getId(), from, to));
    }

    @GetMapping("/incentives")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<IncentiveDto>> incentives(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(earningsService.getIncentives(principal.getId()));
    }

    @GetMapping("/performance")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<PerformanceDto> performance(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(earningsService.getPerformance(principal.getId()));
    }

    @PostMapping("/earnings/withdraw")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal UserPrincipal principal) {
        earningsService.requestWithdrawal(principal.getId());
        return ResponseEntity.noContent().build();
    }

    // ── 6.8 Documents ─────────────────────────────────────────────────────

    @GetMapping("/documents")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<DriverDocumentDto>> getDocuments(@AuthenticationPrincipal UserPrincipal principal) {
        var dp = driverProfileService.getDriverProfile(principal.getId());
        return ResponseEntity.ok(driverDocumentService.getDocuments(dp.getId()));
    }

    @PostMapping(value = "/documents", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverDocumentDto> uploadDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String type,
            @RequestParam MultipartFile file) {
        var dp = driverProfileService.getDriverProfile(principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(driverDocumentService.upload(dp.getId(), type, file));
    }

    // ── 6.9 Surge Zones ───────────────────────────────────────────────────

    @GetMapping("/zones")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<ZoneDto>> zones() {
        return ResponseEntity.ok(zoneService.getDemoZones());
    }

    // ── 6.10 Payout Details ───────────────────────────────────────────────

    @GetMapping("/payout")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<PayoutDetailsDto> getPayout(@AuthenticationPrincipal UserPrincipal principal) {
        var result = driverProfileService.getPayoutDetails(principal.getId());
        return ResponseEntity.ok(PayoutDetailsDto.builder()
                .bankName(result.bankName())
                .accountNumber(result.accountNumber())
                .accountHolder(result.accountHolder())
                .build());
    }

    @PutMapping("/payout")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<PayoutDetailsDto> updatePayout(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        var result = driverProfileService.updatePayoutDetails(
                principal.getId(),
                body.get("bankName"),
                body.get("accountNumber"),
                body.get("accountHolder")
        );
        return ResponseEntity.ok(PayoutDetailsDto.builder()
                .bankName(result.bankName())
                .accountNumber(result.accountNumber())
                .accountHolder(result.accountHolder())
                .build());
    }
}
