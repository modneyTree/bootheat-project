package com.example.bootheat.dto;

import java.time.Instant;
import java.util.List;

public record OrderDetailResponse(
        Long orderId, String orderCode, String status,
        Integer totalAmount, Instant createdAt, Instant approvedAt,
        List<Line> items, Payment payment, Long visitId
) {
    public record Line(Long menuItemId, String name, Integer unitPrice, Integer quantity, Boolean isFinished) {}

    public record Payment(String payerName, Integer amount) {}
}
