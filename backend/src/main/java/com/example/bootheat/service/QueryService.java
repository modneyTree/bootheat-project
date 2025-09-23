package com.example.bootheat.service;

import com.example.bootheat.domain.*;
import com.example.bootheat.dto.OrderDetailManagerResponse;
import com.example.bootheat.dto.OrderDetailResponse;
import com.example.bootheat.dto.TableContextResponse;
import com.example.bootheat.dto.TableInfoResponse;
import com.example.bootheat.repository.*;
import com.example.bootheat.support.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryService {
    private final BoothTableRepository tableRepo;
    private final MenuItemRepository menuRepo;
    private final TableVisitRepository visitRepo;
    private final CustomerOrderRepository orderRepo;
    private final PaymentInfoRepository paymentRepo;
    private final OrderItemRepository orderItemRepo;

    public TableInfoResponse getTableInfo(Long boothId, Integer tableNo) {
        var table = tableRepo.findByBooth_BoothIdAndTableNumber(boothId, tableNo)
                .orElseThrow(() -> new IllegalArgumentException("TABLE_NOT_FOUND"));

        var menus = menuRepo
                .findByBooth_BoothIdAndAvailableTrueOrderByNameAsc(boothId) // ★ 변경
                .stream()
                .map(m -> new TableInfoResponse.Menu(
                        m.getMenuItemId(), m.getName(), m.getPrice(), m.getAvailable(),
                        m.getCategory()==null?null:m.getCategory().name()
                ))
                .toList();

        return new TableInfoResponse(boothId, table.getTableNumber(), menus);
    }

    public TableContextResponse getTableContext(Long boothId, Integer tableNo) {
        BoothTable table = tableRepo.findByBooth_BoothIdAndTableNumber(boothId, tableNo)
                .orElseThrow(() -> new IllegalArgumentException("TABLE_NOT_FOUND"));

        // 현재 OPEN visit (없을 수 있음)
        TableVisit v = visitRepo
                .findFirstByTable_TableIdAndStatusOrderByStartedAtDesc(table.getTableId(), Status.OPEN)
                .orElse(null);

        TableContextResponse.Visit visitDto = null;
        if (v != null) {
            visitDto = new TableContextResponse.Visit(
                    v.getVisitId(),
                    v.getVisitNo(),
                    v.getStatus(),
                    v.getStartedAt().atZone(ZoneId.systemDefault()).toInstant(),
                    v.getClosedAt() == null ? null : v.getClosedAt().atZone(ZoneId.systemDefault()).toInstant()
            );
        }

        var rows = orderRepo.findTop10ByTable_TableIdOrderByCreatedAtDesc(table.getTableId())
                .stream()
                .map(o -> new TableContextResponse.OrderRow(
                        o.getOrderId(),
                        o.getOrderCode(),
                        o.getStatus(),
                        o.getTotalAmount(),
                        o.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                        o.getApprovedAt() == null ? null : o.getApprovedAt().atZone(ZoneId.systemDefault()).toInstant(),
                        o.getVisit().getVisitId()
                ))
                .toList();

        return new TableContextResponse(boothId, tableNo, visitDto, rows);
    }

    @Transactional(readOnly = true)
    public List<TableContextResponse.OrderRow> getLatestVisitOrders(Long tableId) {
        // 1) OPEN 방문 우선, 없으면 startedAt 기준 최신 방문
        var visit = visitRepo
                .findFirstByTable_TableIdAndStatusOrderByStartedAtDesc(tableId, Status.OPEN)
                .orElseGet(() -> visitRepo.findTopByTable_TableIdOrderByStartedAtDesc(tableId).orElse(null));

        if (visit == null) {
            // 방문 자체가 한 번도 없으면 빈 배열 반환
            return java.util.List.of();
        }

        var zone = ZoneId.systemDefault();
        return orderRepo.findByVisit_VisitIdOrderByCreatedAtDesc(visit.getVisitId())
                .stream()
                .map(o -> new TableContextResponse.OrderRow(
                        o.getOrderId(),
                        o.getOrderCode(),
                        o.getStatus(),
                        o.getTotalAmount(),
                        o.getCreatedAt().atZone(zone).toInstant(),
                        o.getApprovedAt() == null ? null : o.getApprovedAt().atZone(zone).toInstant(),
                        o.getVisit().getVisitId()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TableContextResponse.OrderRow> getTableOrders(Long boothId, Long tableId) {
        // 테이블 소속 검증
        var table = tableRepo.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("TABLE_NOT_FOUND"));
        if (!table.getBooth().getBoothId().equals(boothId)) {
            throw new IllegalArgumentException("BOOTH_TABLE_MISMATCH");
        }

        var zone = ZoneId.systemDefault();
        return orderRepo.findByBooth_BoothIdAndTable_TableIdOrderByCreatedAtDesc(boothId, tableId)
                .stream()
                .map(o -> new TableContextResponse.OrderRow(
                        o.getOrderId(),
                        o.getOrderCode(),
                        o.getStatus(),
                        o.getTotalAmount(),
                        o.getCreatedAt().atZone(zone).toInstant(),
                        o.getApprovedAt() == null ? null : o.getApprovedAt().atZone(zone).toInstant(),
                        o.getVisit().getVisitId()
                ))
                .toList();
    }

    // service/QueryService.java (메서드 추가)
    @Transactional(readOnly = true)
    public OrderDetailManagerResponse getOrderDetailForManager(Long orderId) {
        var o = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));

        var items = orderItemRepo.findByOrder_OrderId(orderId).stream()
                .map(i -> new OrderDetailManagerResponse.OrderItemRow(
                        i.getOrderItemId(),                  // ⬅️ 추가
                        i.getMenuItem().getName(),
                        i.getQuantity(),
                        i.getIsFinished()
                ))
                .toList();

        var p = paymentRepo.findByOrder_OrderId(orderId).orElse(null);

        var zone = ZoneId.systemDefault();
        var co = new OrderDetailManagerResponse.CustomerOrderData(
                o.getOrderId(),
                o.getTable().getTableId(),
                o.getVisit().getVisitId(),
                o.getStatus(),
                o.getOrderCode(),
                o.getTotalAmount(),
                o.getCreatedAt().atZone(zone).toInstant(),
                o.getApprovedAt() == null ? null : o.getApprovedAt().atZone(zone).toInstant()
        );
        var pay = (p == null) ? null
                : new OrderDetailManagerResponse.PaymentInfoData(p.getPayerName(), p.getAmount());

        return new OrderDetailManagerResponse(co, items, pay);
    }

    @Transactional(readOnly = true)
    public List<OrderDetailManagerResponse> getTableOrderDetails(Long boothId, Long tableId) {
        var table = tableRepo.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("TABLE_NOT_FOUND"));
        if (!table.getBooth().getBoothId().equals(boothId))
            throw new IllegalArgumentException("BOOTH_TABLE_MISMATCH");

        return orderRepo.findByBooth_BoothIdAndTable_TableIdOrderByCreatedAtDesc(boothId, tableId)
                .stream()
                .map(o -> {
                    var items = orderItemRepo.findByOrder_OrderId(o.getOrderId()).stream()
                            .map(i -> new OrderDetailManagerResponse.OrderItemRow(
                                    i.getOrderItemId(),
                                    i.getMenuItem().getName(),
                                    i.getQuantity(),
                                    i.getIsFinished()
                            ))
                            .toList();
                    var p = paymentRepo.findByOrder_OrderId(o.getOrderId()).orElse(null);
                    var zone = ZoneId.systemDefault();
                    var co = new OrderDetailManagerResponse.CustomerOrderData(
                            o.getOrderId(),
                            o.getTable().getTableId(),
                            o.getVisit().getVisitId(),
                            o.getStatus(),
                            o.getOrderCode(),
                            o.getTotalAmount(),
                            o.getCreatedAt().atZone(zone).toInstant(),
                            o.getApprovedAt() == null ? null : o.getApprovedAt().atZone(zone).toInstant()
                    );
                    var pay = (p == null) ? null
                            : new OrderDetailManagerResponse.PaymentInfoData(p.getPayerName(), p.getAmount());
                    return new OrderDetailManagerResponse(co, items, pay);
                }).toList();
    }

    public List<OrderDetailResponse> findLatestVisitOrders(Long boothId, Long tableId, int limit) {
        // 1) 테이블 소속 검증
        BoothTable table = tableRepo.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("TABLE_NOT_FOUND"));
        if (!Objects.equals(table.getBooth().getBoothId(), boothId)) {
            throw new IllegalArgumentException("BOOTH_TABLE_MISMATCH");
        }

        // 2) 방문 결정: OPEN 우선, 없으면 최신 방문
        TableVisit visit = visitRepo
                .findFirstByTable_TableIdAndStatusOrderByStartedAtDesc(tableId, Status.OPEN)
                .orElseGet(() -> visitRepo.findTopByTable_TableIdOrderByStartedAtDesc(tableId).orElse(null));

        if (visit == null) return List.of(); // 방문 없으면 빈 배열

        // 3) 주문 상위 N개
        var orders = orderRepo
                .findByVisit_VisitIdOrderByCreatedAtDesc(visit.getVisitId(), PageRequest.of(0, Math.max(1, limit)))
                .getContent();
        if (orders.isEmpty()) return List.of();

        // 4) 아이템 배치 로딩
        var orderIds = orders.stream().map(CustomerOrder::getOrderId).toList();
        var items = orderItemRepo.findByOrder_OrderIdIn(orderIds);

        // orderId -> items 묶기
        Map<Long, List<OrderItem>> itemsByOrder = items.stream()
                .collect(Collectors.groupingBy(oi -> oi.getOrder().getOrderId()));

        // (옵션) 결제정보 배치 로딩
        Map<Long, PaymentInfo> payByOrder = paymentRepo instanceof PaymentInfoRepository
                ? paymentRepo.findByOrder_OrderIdIn(orderIds)
                .stream().collect(Collectors.toMap(pi -> pi.getOrder().getOrderId(), pi -> pi))
                : Collections.emptyMap();

        var zone = ZoneId.systemDefault();
        List<OrderDetailResponse> result = new ArrayList<>(orders.size());
        for (var o : orders) {
            var lineDtos = itemsByOrder.getOrDefault(o.getOrderId(), List.of())
                    .stream()
                    .map(i -> new OrderDetailResponse.Line(
                            i.getMenuItem().getMenuItemId(),
                            i.getMenuItem().getName(),
                            i.getUnitPrice(),
                            i.getQuantity(),
                            i.getIsFinished()
                    ))
                    .toList();

            var p = payByOrder.isEmpty()
                    ? paymentRepo.findByOrder_OrderId(o.getOrderId()).orElse(null) // N+1 가능
                    : payByOrder.get(o.getOrderId());

            var payDto = (p == null) ? null : new OrderDetailResponse.Payment(p.getPayerName(), p.getAmount());

            result.add(new OrderDetailResponse(
                    o.getOrderId(),
                    o.getOrderCode(),
                    o.getStatus(),
                    o.getTotalAmount(),
                    o.getCreatedAt().atZone(zone).toInstant(),
                    o.getApprovedAt() == null ? null : o.getApprovedAt().atZone(zone).toInstant(),
                    lineDtos,
                    payDto,
                    o.getVisit() == null ? null : o.getVisit().getVisitId()
            ));
        }
        return result;
    }

    public List<OrderDetailResponse> findOrdersByVisit(Long boothId, Long tableId, Long visitId, int limit) {
        // 1) 테이블/부스 검증
        BoothTable table = tableRepo.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("TABLE_NOT_FOUND"));
        if (!Objects.equals(table.getBooth().getBoothId(), boothId)) {
            throw new IllegalArgumentException("BOOTH_TABLE_MISMATCH");
        }

        // 2) 해당 visitId의 상위 N개 주문
        var orders = orderRepo
                .findByVisit_VisitIdOrderByCreatedAtDesc(visitId, PageRequest.of(0, Math.max(1, limit)))
                .getContent();
        if (orders.isEmpty()) return List.of();

        // 3) 아이템/결제 동일 로직
        var orderIds = orders.stream().map(CustomerOrder::getOrderId).toList();
        var items = orderItemRepo.findByOrder_OrderIdIn(orderIds);
        Map<Long, List<OrderItem>> itemsByOrder = items.stream()
                .collect(Collectors.groupingBy(oi -> oi.getOrder().getOrderId()));

        Map<Long, PaymentInfo> payByOrder = paymentRepo instanceof PaymentInfoRepository
                ? paymentRepo.findByOrder_OrderIdIn(orderIds)
                .stream().collect(Collectors.toMap(pi -> pi.getOrder().getOrderId(), pi -> pi))
                : Collections.emptyMap();

        var zone = ZoneId.systemDefault();
        List<OrderDetailResponse> result = new ArrayList<>(orders.size());
        for (var o : orders) {
            var lineDtos = itemsByOrder.getOrDefault(o.getOrderId(), List.of())
                    .stream()
                    .map(i -> new OrderDetailResponse.Line(
                            i.getMenuItem().getMenuItemId(),
                            i.getMenuItem().getName(),
                            i.getUnitPrice(),
                            i.getQuantity(),
                            i.getIsFinished()
                    ))
                    .toList();

            var p = payByOrder.isEmpty()
                    ? paymentRepo.findByOrder_OrderId(o.getOrderId()).orElse(null)
                    : payByOrder.get(o.getOrderId());

            var payDto = (p == null) ? null : new OrderDetailResponse.Payment(p.getPayerName(), p.getAmount());

            result.add(new OrderDetailResponse(
                    o.getOrderId(),
                    o.getOrderCode(),
                    o.getStatus(),
                    o.getTotalAmount(),
                    o.getCreatedAt().atZone(zone).toInstant(),
                    o.getApprovedAt() == null ? null : o.getApprovedAt().atZone(zone).toInstant(),
                    lineDtos,
                    payDto,
                    o.getVisit() == null ? null : o.getVisit().getVisitId()
            ));
        }
        return result;
    }
}
