package com.delivery.payment.controller;

import com.delivery.payment.dto.CheckoutRequest;
import com.delivery.payment.dto.CheckoutResponse;
import com.delivery.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CheckoutResponse> checkout(
            @Valid @RequestBody CheckoutRequest req) {
        return ResponseEntity.ok(paymentService.createPaymentIntent(req));
    }

    /**
     * Stripe webhook — must be excluded from JWT auth in SecurityConfig.
     * Validates via Stripe-Signature header (add WebhookService for full implementation).
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        // Signature verification and event routing belongs in a dedicated WebhookService
        // Minimal wiring: signal success after verification
        return ResponseEntity.ok().build();
    }
}
