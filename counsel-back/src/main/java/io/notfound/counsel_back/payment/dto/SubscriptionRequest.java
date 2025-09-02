package io.notfound.counsel_back.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @Min(value = 100, message = "결제 금액은 최소 100원 이상이어야 합니다.")
    private int amount;

    @NotNull(message = "고객 고유 번호는 필수입니다.")
    @Size(min = 1, message = "고객 고유 번호는 비어있을 수 없습니다.")
    private String customerUid;
}