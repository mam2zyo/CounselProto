package io.notfound.counsel_back.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private int amount;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    @Embedded
    private BillingKey billingKey;

    private LocalDate nextPaymentDate;

    public Subscription(Long userId, int amount, BillingKey billingKey) {
        this.userId = userId;
        this.amount = amount;
        this.status = SubscriptionStatus.ACTIVE;
        this.billingKey = billingKey;
        this.nextPaymentDate = LocalDate.now().plusMonths(1);
    }
}