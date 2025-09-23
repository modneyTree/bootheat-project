package com.example.bootheat.service;

import com.example.bootheat.domain.BoothTable;
import com.example.bootheat.domain.TableVisit;
import com.example.bootheat.dto.OrderDetailManagerResponse;
import com.example.bootheat.dto.TableContextResponse;
import com.example.bootheat.dto.TableInfoResponse;
import com.example.bootheat.repository.*;
import com.example.bootheat.support.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;

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
}
