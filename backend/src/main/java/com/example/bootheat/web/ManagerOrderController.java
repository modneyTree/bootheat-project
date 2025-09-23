package com.example.bootheat.web;

import com.example.bootheat.dto.UpdateOrderStatusRequest;
import com.example.bootheat.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerOrderController {
    private final OrderService orderService;

    // web/ManagerOrderController.java
    @PostMapping("/orders/{orderId}/status/{status}")
    public ResponseEntity<Void> change(
            @PathVariable Long orderId,
            @PathVariable String status,
            @RequestBody(required = false) UpdateOrderStatusRequest req
    ) {
        // 바디가 오면 유효성 체크(선택)
        if (req != null) {
            if (req.order_id() != null && !req.order_id().equals(orderId)) {
                throw new IllegalArgumentException("ORDER_ID_MISMATCH");
            }
            if (req.status() != null && !req.status().isBlank() &&
                    !req.status().equalsIgnoreCase(status)) {
                throw new IllegalArgumentException("STATUS_MISMATCH");
            }
        }
        orderService.changeStatus(orderId, status);
        return ResponseEntity.ok().build(); // 200
    }

    // ✅ (신규) 특정 라인아이템 완료/취소
    // 예: PATCH /api/manager/orders/123/items/456/finished?finished=true
    @PatchMapping("/orders/{orderId}/items/{orderItemId}/finished")
    public ResponseEntity<Void> setItemFinished(
            @PathVariable Long orderId,
            @PathVariable Long orderItemId,
            @RequestParam boolean finished
    ) {
        orderService.setOrderItemFinished(orderId, orderItemId, finished);
        return ResponseEntity.ok().build();
    }

    // ✅ (신규) 주문의 모든 라인아이템 일괄 완료/취소
    // 예: PATCH /api/manager/orders/123/items/finished?finished=true
    @PatchMapping("/orders/{orderId}/items/finished")
    public ResponseEntity<Integer> setAllItemsFinished(
            @PathVariable Long orderId,
            @RequestParam boolean finished
    ) {
        int updated = orderService.setAllOrderItemsFinished(orderId, finished);
        return ResponseEntity.ok(updated); // 변경된 row 수 반환
    }

}
