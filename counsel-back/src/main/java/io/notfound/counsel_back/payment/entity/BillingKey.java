package io.notfound.counsel_back.payment.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BillingKey {

    private String value;
    private String customerUid;

    public BillingKey(String value, String customerUid) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("빌링키는 필수 값입니다.");
        }
        this.value = value;
        this.customerUid = customerUid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillingKey that = (BillingKey) o;
        return Objects.equals(value, that.value) && Objects.equals(customerUid, that.customerUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, customerUid);
    }
}