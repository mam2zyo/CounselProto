package io.notfound.counsel_back.payment.service;

import io.notfound.counsel_back.payment.dto.SubscriptionRequest;
import io.notfound.counsel_back.payment.dto.SubscriptionResponse;
import io.notfound.counsel_back.payment.entity.BillingKey;
import io.notfound.counsel_back.payment.entity.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PortoneApiAdapter portoneApiAdapter;

    @Transactional
    public SubscriptionResponse startSubscription(SubscriptionRequest request) {
        // 1. 외부 API를 통해 빌링키 발급
        BillingKey billingKey = portoneApiAdapter.requestBillingKey(request.getCustomerUid());

        // 2. DTO를 엔티티로 변환
        Subscription newSubscription = new Subscription(
                request.getUserId(),
                request.getAmount(),
                billingKey
        );

        // 3. 리포지토리를 통해 엔티티 저장
        Subscription savedSubscription = subscriptionRepository.save(newSubscription);

        // 4. 엔티티를 응답 DTO로 변환하여 반환
        SubscriptionResponse response = new SubscriptionResponse();
        response.setSubscriptionId(savedSubscription.getId());
        response.setUserId(savedSubscription.getUserId());
        response.setStatus(savedSubscription.getStatus().name());
        response.setAmount(savedSubscription.getAmount());
        response.setNextPaymentDate(savedSubscription.getNextPaymentDate());

        return response;
    }
}