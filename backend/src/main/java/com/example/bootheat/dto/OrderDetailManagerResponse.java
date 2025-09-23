// dto/OrderDetailManagerResponse.java
package com.example.bootheat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record OrderDetailManagerResponse(
        @JsonProperty("customerOrder") CustomerOrderData customerOrder,
        @JsonProperty("orderItems") List<OrderItemRow> orderItems,
        @JsonProperty("paymentInfo") PaymentInfoData paymentInfo
) {
    public record CustomerOrderData(
            @JsonProperty("order_id") Long orderId,
            @JsonProperty("table_id") Long tableId,
            @JsonProperty("visit_id") Long visitId,
            @JsonProperty("status") String status,
            @JsonProperty("order_code") String orderCode,
            @JsonProperty("total_amount") Integer totalAmount,
            @JsonProperty("created_at") Instant createdAt,
            @JsonProperty("approved_at") Instant approvedAt
    ) {}

    public record OrderItemRow(
            @JsonProperty("order_item_id") Long orderItemId,   // ⬅️ 추가
            @JsonProperty("name") String name,
            @JsonProperty("quantity") Integer quantity,
            @JsonProperty("is_finished") Boolean isFinished    // ⬅️ snake_case 유지
    ) {}

    public record PaymentInfoData(
            @JsonProperty("payer_name") String payerName,
            @JsonProperty("amount") Integer amount
    ) {}
}
