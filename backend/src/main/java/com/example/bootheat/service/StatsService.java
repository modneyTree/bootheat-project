package com.example.bootheat.service;

import com.example.bootheat.domain.CustomerOrder;
import com.example.bootheat.domain.OrderItem;
import com.example.bootheat.dto.*;
import com.example.bootheat.repository.CustomerOrderRepository;
import com.example.bootheat.repository.OrderItemRepository;
import com.example.bootheat.repository.TableVisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final CustomerOrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final TableVisitRepository tableVisitRepo;

    private static class Range {
        final LocalDate date;   // 예시: 2023-10-01
        final LocalDateTime start;  // 예시: 2023-10-01T00:00
        final LocalDateTime end;
        Range(LocalDate date, ZoneId zone) {
            this.date = date;
            this.start = date.atStartOfDay();   // 예시: 2023-10-01T00:00
            this.end = this.start.plusDays(1);
        }
    }

    // 오늘 날짜 범위 생성
    private Range todayRange() {
        var zone = ZoneId.systemDefault();  // 시스템 기본 시간대 사용
        var date = LocalDate.now(zone); // 오늘 날짜 (예시: 2023-10-01)
        return new Range(date, zone);
    }

    // 오늘 통계 조회
    public TodayStatsResponse todayStats(Long boothId, int topN) {
        var r = todayRange();

        // 총 주문수/총액
        Object[] sum = orderRepo.sumToday(boothId, r.start, r.end);
        long totalOrders = (sum[0] == null) ? 0L : ((Number) sum[0]).longValue();
        long totalAmount = (sum[1] == null) ? 0L : ((Number) sum[1]).longValue();

        // 시간대별 → 피크아워
        Integer peakHour = null;
        var buckets = orderRepo.hourlyCounts(boothId, r.start, r.end);
        if (!buckets.isEmpty()) {
            peakHour = buckets.stream()
                    .max(Comparator.comparingLong(o -> ((Number)o[1]).longValue()))
                    .map(o -> ((Number) o[0]).intValue())
                    .orElse(null);
        }

        // 메뉴 Top N (qty 기준 기본 정렬)
        var rows = orderItemRepo.aggregateMenuToday(boothId, r.start, r.end);
        List<MenuTopItem> all = rows.stream()
                .map(o -> new MenuTopItem(
                        ((Number)o[0]).longValue(),
                        (String) o[1],
                        ((Number)o[2]).longValue(),
                        ((Number)o[3]).longValue()
                ))
                .sorted(Comparator.comparingLong(MenuTopItem::qty).reversed()
                        .thenComparingLong(MenuTopItem::amount).reversed())
                .toList();
        List<MenuTopItem> top = all.size() > topN ? all.subList(0, topN) : all;

        return new TodayStatsResponse(boothId, r.date, totalOrders, totalAmount, peakHour, top);
    }

    // 메뉴 판매 순위 조회
    // metric: "qty" 또는 "amount"
    public MenuRankingResponse ranking(Long boothId, String metric, int limit) {
        var r = todayRange();
        var rows = orderItemRepo.aggregateMenuToday(boothId, r.start, r.end);
        Comparator<MenuTopItem> cmp = "amount".equalsIgnoreCase(metric)
                ? Comparator.comparingLong(MenuTopItem::amount).reversed()
                : Comparator.comparingLong(MenuTopItem::qty).reversed();

        List<MenuTopItem> all = rows.stream()
                .map(o -> new MenuTopItem(
                        ((Number)o[0]).longValue(),
                        (String) o[1],
                        ((Number)o[2]).longValue(),
                        ((Number)o[3]).longValue()
                ))
                .sorted(cmp.thenComparing(MenuTopItem::name))
                .toList();

        List<MenuTopItem> items = all.size() > limit ? all.subList(0, limit) : all;
        return new MenuRankingResponse(boothId, r.date, metric, items);
    }

    // 날짜별 통계 조회
    @Transactional(readOnly = true)
    public TodayStatsResponse statsByDate(Long boothId, LocalDate date) {
        var start = date.atStartOfDay();
        var end = start.plusDays(1);

        var r = todayRange();
        var totals = orderRepo.sumBetween(boothId, r.start, r.end);
        long totalOrders = (totals == null) ? 0L : totals.orders();
        long totalAmount = (totals == null) ? 0L : totals.sales();

        var buckets = orderRepo.hourlyCountsBetween(boothId, r.start, r.end);

        Integer peakHour = null;
        if (!buckets.isEmpty()) {
            peakHour = buckets.stream()
                    .max(java.util.Comparator.comparingLong(o -> ((Number)o[1]).longValue()))
                    .map(o -> ((Number)o[0]).intValue())
                    .orElse(null);
        }

        var rows = orderItemRepo.aggregateMenuBetween(boothId, start, end);
        var all = rows.stream()
                .map(o -> new MenuTopItem(((Number)o[0]).longValue(), (String)o[1],
                        ((Number)o[2]).longValue(), ((Number)o[3]).longValue()))
                .sorted(java.util.Comparator.comparingLong(MenuTopItem::qty).reversed()
                        .thenComparingLong(MenuTopItem::amount).reversed())
                .toList();
        var top = all.size()>5 ? all.subList(0,5) : all;
        return new TodayStatsResponse(boothId, date, totalOrders, totalAmount, peakHour, top);
    }

    // 기능: 특정 부스의 메뉴 아이템 총 주문 수 조회
    @Transactional(readOnly = true)
    public long totalOrdersForMenu(Long boothId, Long menuItemId) {
        return orderItemRepo.totalQtyByBoothAndMenu(boothId, menuItemId);
    }

    // 기능: 특정 부스의 메뉴 판매 통계 조회
    @Transactional(readOnly = true)
    public MenuRankingResponse menuSales(Long boothId, LocalDate date) {
        var d = (date==null) ? java.time.LocalDate.now() : date;
        var start = d.atStartOfDay();
        var end = start.plusDays(1);
        var rows = orderItemRepo.aggregateMenuBetween(boothId, start, end);
        var items = rows.stream()
                .map(o -> new MenuTopItem(((Number)o[0]).longValue(), (String)o[1],
                        ((Number)o[2]).longValue(), ((Number)o[3]).longValue()))
                .sorted(java.util.Comparator.comparingLong(MenuTopItem::qty).reversed()
                        .thenComparing(MenuTopItem::name))
                .toList();
        return new MenuRankingResponse(boothId, d, "qty", items);
    }
    // service/StatsService.java (메서드 추가)
    // 기능: 날짜별 통계 요약 조회
    @Transactional(readOnly = true)
    public StatsSummaryResponse statsSummaryByDate(Long boothId, LocalDate date) {
        var start = date.atStartOfDay();
        var end   = start.plusDays(1);

        var totals = orderRepo.sumBetween(boothId, start, end); // StatsTotals
        long orders = (totals == null) ? 0L : totals.orders();
        long sales  = (totals == null) ? 0L : totals.sales();

        return new StatsSummaryResponse(date.toString(), sales, orders);
    }


    // 기능: 특정 부스의 메뉴 판매 통계 조회 (메뉴별 판매량)
    @Transactional(readOnly = true)
    public java.util.List<MenuSalesItem> menuSalesItems(Long boothId, java.time.LocalDate date) {
        var d = (date==null) ? java.time.LocalDate.now() : date;
        var start = d.atStartOfDay();
        var end = start.plusDays(1);
        var rows = orderItemRepo.aggregateMenuBetween(boothId, start, end);
        return rows.stream()
                .map(o -> new MenuSalesItem(
                        ((Number)o[0]).longValue(),
                        (String) o[1],
                        ((Number)o[3]).longValue() // amount
                ))
                .sorted(java.util.Comparator.comparingLong(MenuSalesItem::totalSales).reversed())
                .toList();
    }

    public Map<Long, List<OrderWithItemsDto>> allBoothsOrdersByDate(LocalDate date) {
        // 날짜 범위: [start, end)
        var start = date.atStartOfDay();
        var end = start.plusDays(1);

        // 모든 부스의 해당일 주문
        List<CustomerOrder> orders = orderRepo
                .findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByBooth_BoothIdAscCreatedAtAsc(start, end);
        if (orders.isEmpty()) return Collections.emptyMap();

        // 주문ID 모아 라인아이템 일괄 조회 (menuItem까지 fetch)
        List<Long> orderIds = orders.stream().map(CustomerOrder::getOrderId).toList();
        List<OrderItem> items = orderItemRepo.findByOrder_OrderIdIn(orderIds);

        // 주문ID -> 라인아이템 리스트 매핑
        Map<Long, List<OrderItem>> itemsByOrderId = items.stream()
                .collect(Collectors.groupingBy(oi -> oi.getOrder().getOrderId()));

        ZoneId zone = ZoneId.systemDefault();

        // boothId -> 주문DTO 리스트 그룹핑
        return orders.stream()
                .map(o -> {
                    List<OrderItemBrief> lines = itemsByOrderId.getOrDefault(o.getOrderId(), List.of())
                            .stream()
                            .map(oi -> new OrderItemBrief(
                                    oi.getMenuItem().getMenuItemId(),
                                    oi.getMenuItem().getName(),
                                    oi.getUnitPrice(),
                                    oi.getQuantity(),
                                    (long) oi.getUnitPrice() * oi.getQuantity(),
                                    oi.getIsFinished()
                            ))
                            .toList();

                    return new OrderWithItemsDto(
                            o.getOrderId(),
                            o.getBooth().getBoothId(),
                            o.getTotalAmount(),
                            o.getCreatedAt().atZone(zone).toInstant(),
                            lines
                    );
                })
                .collect(Collectors.groupingBy(OrderWithItemsDto::boothId, LinkedHashMap::new, Collectors.toList()));
    }

    public AllBoothsSummaryResponse allBoothsSummaryByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end   = start.plusDays(1);

        StatsTotals t = orderRepo.sumAllBetween(start, end);
        long orders = (t == null) ? 0L : t.orders();
        long sales  = (t == null) ? 0L : t.sales();

        return AllBoothsSummaryResponse.of(sales, orders);
    }

    public List<Long> visitDurationsByDate(LocalDate date) {
        var start = date.atStartOfDay();
        var end   = start.plusDays(1);

        var visits = tableVisitRepo.findClosedVisitsBetween(start, end);
        return visits.stream()
                .map(v -> Duration.between(v.getStartedAt(), v.getClosedAt()).toMinutes())
                .toList();
    }
}