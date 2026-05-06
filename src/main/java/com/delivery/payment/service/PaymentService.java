package com.delivery.payment.service;

import com.delivery.common.exception.AuthException;
import com.delivery.order.domain.Order;
import com.delivery.order.domain.OrderStatus;
import com.delivery.order.repository.OrderRepository;
import com.delivery.order.service.OrderService;
import com.delivery.payment.domain.PaymentStatus;
import com.delivery.payment.domain.Transaction;
import com.delivery.payment.dto.CheckoutRequest;
import com.delivery.payment.dto.CheckoutResponse;
import com.delivery.payment.repository.TransactionRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Value("${app.stripe.secret-key}")
    private String stripeSecretKey;

    @PostConstruct
    void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Transactional
    public CheckoutResponse createPaymentIntent(CheckoutRequest req) {
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AuthException("Order is not in PENDING state");
        }

        long amountCents = order.getTotal().multiply(java.math.BigDecimal.valueOf(100)).longValue();

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountCents)
                    .setCurrency("usd")
                    .addPaymentMethodType(req.getPaymentMethodType() != null ? req.getPaymentMethodType() : "card")
                    .putMetadata("orderId", order.getId().toString())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            Transaction transaction = Transaction.builder()
                    .orderId(order.getId())
                    .stripePaymentIntentId(intent.getId())
                    .amount(order.getTotal())
                    .method(req.getPaymentMethodType() != null ? req.getPaymentMethodType() : "card")
                    .status(PaymentStatus.PENDING)
                    .build();
            Transaction saved = transactionRepository.save(transaction);

            return CheckoutResponse.builder()
                    .transactionId(saved.getId())
                    .paymentIntentId(intent.getId())
                    .clientSecret(intent.getClientSecret())
                    .amount(order.getTotal())
                    .currency("usd")
                    .build();

        } catch (StripeException e) {
            log.error("Stripe error for order {}: {}", order.getId(), e.getMessage());
            throw new RuntimeException("Payment initiation failed: " + e.getMessage());
        }
    }

    @Transactional
    public void handleWebhookSuccess(String paymentIntentId) {
        transactionRepository.findByStripePaymentIntentId(paymentIntentId).ifPresent(tx -> {
            tx.setStatus(PaymentStatus.SUCCEEDED);
            transactionRepository.save(tx);
            orderService.updateStatus(tx.getOrderId(), OrderStatus.CONFIRMED);
        });
    }
}
