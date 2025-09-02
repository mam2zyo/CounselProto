package io.notfound.counsel_back.payment.controller;

import io.notfound.counsel_back.payment.dto.SubscriptionRequest;
import io.notfound.counsel_back.payment.dto.SubscriptionResponse;
import io.notfound.counsel_back.payment.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public ResponseEntity<SubscriptionResponse> startSubscription(
            @RequestBody @Valid SubscriptionRequest request
    ) {
        SubscriptionResponse response = subscriptionService.startSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}