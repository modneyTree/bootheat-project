package com.example.bootheat.service;

import com.example.bootheat.domain.*;
import com.example.bootheat.dto.*;
import com.example.bootheat.repository.*;
import com.example.bootheat.support.Status;
import com.example.bootheat.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final BoothRepository boothRepo;
    private final BoothTableRepository tableRepo;
    private final MenuItemRepository menuRepo;
    private final TableVisitRepository visitRepo;       // NEW
    private final CustomerOrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final PaymentInfoRepository paymentRepo;

    // service/OrderService.java (createOrder 수정)
    @Transactional
    public OrderCreatedResponse createOrder(CreateOrderRequest req) {
        Booth booth = boothRepo.findById(req.boothId())
                .orElseThrow(() -> new IllegalArgumentException("BOOTH_NOT_FOUND"));
        BoothTable table = tableRepo.findByBooth_BoothIdAndTableNumber(req.boothId(), req.tableNo())
                .orElseThrow(() -> new IllegalArgumentException("TABLE_NOT_FOUND"));

        // OPEN visit 재사용 or 새로 생성
        TableVisit visit = visitRepo
                .findFirstByTable_TableIdAndStatusOrderByStartedAtDesc(table.getTableId(), Status.OPEN)
                .orElseGet(() -> {
                    int nextNo = visitRepo.findTopByTable_TableIdOrderByVisitNoDesc(table.getTableId())
                            .map(v -> v.getVisitNo() + 1).orElse(1);
                    return visitRepo.save(TableVisit.builder()
                            .table(table).visitNo(nextNo).status(Status.OPEN).build());
                });

        // ✅ 클라 값을 신뢰: unitPrice = req.items[*].price
        int computedSum = 0;
        List<OrderItem> lines = new java.util.ArrayList<>();
        for (var it : req.items()) {
            var mi = menuRepo.findById(it.foodId())
                    .orElseThrow(() -> new IllegalArgumentException("MENU_NOT_FOUND"));
            int unit = it.price();               // ★ 클라 가격 사용
            int qty  = it.quantity();
            computedSum += unit * qty;

            OrderItem oi = new OrderItem();
            oi.setMenuItem(mi);                  // FK는 유지 (통계/참조용)
            oi.setQuantity(qty);
            oi.setUnitPrice(unit);               // ★ 클라 가격 저장
            lines.add(oi);
        }

        // ✅ 총액도 클라 payment.amount를 그대로 사용 (할인/프로모션 반영 가정)
        int totalAmount = req.payment().amount();

        CustomerOrder order = new CustomerOrder();
        order.setBooth(booth);
        order.setTable(table);
        order.setVisit(visit);
        order.setStatus(Status.PENDING);         // 내부 상태
        order.setTotalAmount(totalAmount);       // ★ 클라 금액 사용

        orderRepo.saveAndFlush(order);
        order.setOrderCode(CodeGenerator.orderCodeFromId(order.getOrderId()));

        for (OrderItem oi : lines) oi.setOrder(order);
        orderItemRepo.saveAll(lines);

        PaymentInfo pi = new PaymentInfo();
        pi.setOrder(order);
        pi.setPayerName(req.payment().payerName());
        pi.setAmount(totalAmount);               // ★ 클라 금액 저장
        paymentRepo.save(pi);

        // ✅ 응답 status는 스펙 그대로 "PENDDING"
        return new OrderCreatedResponse(
                order.getOrderId(),
                "PENDDING",                      // 의도적 철자 (프론트 계약)
                totalAmount,
                order.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()
        );
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(Long orderId) {
        CustomerOrder o = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));

        var items = orderItemRepo.findByOrder_OrderId(orderId).stream()
                .map(i -> new OrderDetailResponse.Line(
                        i.getMenuItem().getMenuItemId(),
                        i.getMenuItem().getName(),
                        i.getUnitPrice(),
                        i.getQuantity(),
                        i.getIsFinished()))
                .toList();

        var p = paymentRepo.findByOrder_OrderId(orderId).orElse(null);

        return new OrderDetailResponse(
                o.getOrderId(), o.getOrderCode(), o.getStatus(),
                o.getTotalAmount(),
                o.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                o.getApprovedAt() == null ? null : o.getApprovedAt().atZone(ZoneId.systemDefault()).toInstant(),
                items,
                p == null ? null : new OrderDetailResponse.Payment(p.getPayerName(), p.getAmount()),
                (o.getVisit() == null) ? null : o.getVisit().getVisitId()
        );
    }
    // service/OrderService.java
    @Transactional
    public void changeStatus(Long orderId, String raw) {
        String st = raw == null ? "" : raw.trim().toUpperCase();
        // 철자 보정: 'PENDDING'도 PENDING으로 처리
        if ("PENDDING".equals(st)) st = "PENDING";

        switch (st) {
            case "APPROVED" -> approve(orderId);
            case "REJECTED" -> reject(orderId);
            case "PENDING"  -> setPending(orderId);
            case "FINISHED" -> finish(orderId);
            default -> throw new IllegalArgumentException("UNKNOWN_STATUS");
        }
    }

    @Transactional
    public void reject(Long orderId) {
        var o = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));
        if (!Status.PENDING.equals(o.getStatus()))
            throw new IllegalStateException("INVALID_STATE");
        o.setStatus(Status.REJECTED);
    }

    @Transactional
    public void setPending(Long orderId) {
        var o = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));
        // 필요 시 추가 규칙
        o.setStatus(Status.PENDING);
    }

    @Transactional
    public void approve(Long orderId) {
        CustomerOrder o = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));
        if (!Status.PENDING.equals(o.getStatus())) throw new IllegalStateException("INVALID_STATE");
        o.setStatus(Status.APPROVED);
        o.setApprovedAt(java.time.LocalDateTime.now());
    }

    @Transactional
    public void finish(Long orderId) {
        CustomerOrder o = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));
        if (!Status.APPROVED.equals(o.getStatus())) {
            throw new IllegalStateException("INVALID_STATE"); // APPROVED -> FINISHED만 허용
        }
        o.setStatus(Status.FINISHED);

        // ✅ 주문 완료 시 아이템도 완료 처리
        orderItemRepo.updateFinishedAllInOrder(orderId, true);
    }

    // (선택) 테이블 비우기(visit 종료)
    @Transactional
    public void closeCurrentVisit(Long boothId, Integer tableNo) {
        BoothTable table = tableRepo.findByBooth_BoothIdAndTableNumber(boothId, tableNo)
                .orElseThrow(() -> new IllegalArgumentException("TABLE_NOT_FOUND"));
        visitRepo.findFirstByTable_TableIdAndStatusOrderByStartedAtDesc(table.getTableId(), Status.OPEN)
                .ifPresent(v -> {
                    v.setStatus(Status.CLOSED);
                    v.setClosedAt(java.time.LocalDateTime.now());
                });
    }

    @Transactional
    public void closeCurrentVisitByTableId(Long tableId) {
        var table = tableRepo.findById(tableId).orElseThrow(() -> new IllegalArgumentException("TABLE_NOT_FOUND"));
        visitRepo.findFirstByTable_TableIdAndStatusOrderByStartedAtDesc(tableId, Status.OPEN)
                .ifPresent(v -> { v.setStatus(Status.CLOSED); v.setClosedAt(java.time.LocalDateTime.now()); });
    }

    public List<TableContextResponse.OrderRow> getLatestVisitOrders(Long tableId) {
        var visit = visitRepo.findFirstByTable_TableIdAndStatusOrderByStartedAtDesc(tableId, Status.OPEN)
                .orElseGet(() -> visitRepo.findTopByTable_TableIdOrderByStartedAtDesc(tableId)
                        .orElseThrow(() -> new IllegalArgumentException("VISIT_NOT_FOUND")));
        return orderRepo.findByVisit_VisitIdOrderByCreatedAtDesc(visit.getVisitId())
                .stream().map(o -> new TableContextResponse.OrderRow(
                        o.getOrderId(), o.getOrderCode(), o.getStatus(), o.getTotalAmount(),
                        o.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                        o.getApprovedAt() == null ? null : o.getApprovedAt().atZone(ZoneId.systemDefault()).toInstant(),
                        o.getVisit().getVisitId()
                )).toList();
    }

    public List<TableContextResponse.OrderRow> getTableOrders(Long boothId, Long tableId) {
        var table = tableRepo.findById(tableId).orElseThrow(() -> new IllegalArgumentException("TABLE_NOT_FOUND"));
        if (!table.getBooth().getBoothId().equals(boothId))
            throw new IllegalArgumentException("BOOTH_TABLE_MISMATCH");
        return orderRepo.findByBooth_BoothIdAndTable_TableIdOrderByCreatedAtDesc(boothId, tableId)
                .stream().map(o -> new TableContextResponse.OrderRow(
                        o.getOrderId(), o.getOrderCode(), o.getStatus(), o.getTotalAmount(),
                        o.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                        o.getApprovedAt()==null?null:o.getApprovedAt().atZone(ZoneId.systemDefault()).toInstant(),
                        o.getVisit().getVisitId()
                )).toList();
    }
    @Transactional
    public void setOrderItemFinished(Long orderId, Long orderItemId, boolean finished) {
        int updated = orderItemRepo.updateFinishedOne(orderId, orderItemId, finished);
        if (updated == 0) {
            throw new IllegalArgumentException("ORDER_ITEM_NOT_FOUND");
        }
    }

    @Transactional
    public int setAllOrderItemsFinished(Long orderId, boolean finished) {
        return orderItemRepo.updateFinishedAllInOrder(orderId, finished);
    }
}
