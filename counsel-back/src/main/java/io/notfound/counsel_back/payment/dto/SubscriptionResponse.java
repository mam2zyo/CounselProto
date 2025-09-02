package io.notfound.counsel_back.payment.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class SubscriptionResponse {
    private Long subscriptionId;
    private Long userId;
    private String status;
    private int amount;
    private LocalDate nextPaymentDate;
}