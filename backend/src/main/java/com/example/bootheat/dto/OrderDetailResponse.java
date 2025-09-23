package com.example.bootheat.dto;

import java.time.Instant;
import java.util.List;

// dto/OrderDetailResponse.java
public record OrderDetailResponse(
        Long orderId, String orderCode, String status,
        Integer totalAmount, Instant createdAt, Instant approvedAt,
        List<Line> items,
        Payment payment,      // 필요 없으면 null로 내려가도 됨
        Long visitId
) {
    public record Line(
            Long menuItemId,
            String name,
            Integer unitPrice,
            Integer quantity,
            Boolean isFinished   // ⬅️ 추가/유지
    ) {}
    public record Payment(String payerName, Integer amount) {}
}
