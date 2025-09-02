package io.notfound.counsel_back.payment.service;

import io.notfound.counsel_back.payment.config.PortoneProperties;
import io.notfound.counsel_back.payment.entity.BillingKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class PortoneApiAdapter {

    private final PortoneProperties portoneProperties;

    // 포트원 API를 통해 토큰 발급 (PortoneProperties 사용)
    public String getAccessToken() throws IOException {
        // TODO: 포트원 API에 HTTP POST 요청을 보내 토큰을 받아오는 로직 구현
        // 요청 본문에 portoneProperties.getKey() 와 portoneProperties.getSecret() 사용
        // 예시:
        // HttpEntity<TokenRequest> request = new HttpEntity<>(new TokenRequest(portoneProperties.getKey(), portoneProperties.getSecret()));
        // restTemplate.exchange("https://api.iamport.kr/users/getToken", HttpMethod.POST, request, TokenResponse.class);
        return "fake_access_token";
    }

    public BillingKey requestBillingKey(String customerUid) {
        // TODO: 빌링키 발급 API 호출 시, 위에서 발급받은 토큰을 HTTP 헤더에 담아 전송
        // 예시:
        // String accessToken = getAccessToken();
        // HttpHeaders headers = new HttpHeaders();
        // headers.setBearerAuth(accessToken);

        String billingKey = "fake_billing_key_" + System.currentTimeMillis();
        return new BillingKey(billingKey, customerUid);
    }
}