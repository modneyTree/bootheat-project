// src/main/java/com/example/bootheat/repository/OrderItemRepository.java
package com.example.bootheat.repository;

import com.example.bootheat.domain.OrderItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 주문 상세 라인 조회 (OrderService.getOrder)
    List<OrderItem> findByOrder_OrderId(Long orderId);

    // 오늘/기간 집계 (StatsService.todayStats 등)
    @Query("""
           select oi.menuItem.menuItemId,
                  oi.menuItem.name,
                  sum(oi.quantity) as qty,
                  sum(oi.quantity * oi.unitPrice) as amount
             from OrderItem oi
            where oi.order.booth.boothId = :boothId
              and oi.order.createdAt between :start and :end
            group by oi.menuItem.menuItemId, oi.menuItem.name
           """)
    List<Object[]> aggregateMenuToday(@Param("boothId") Long boothId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    // (확장) 동일 쿼리를 범용 이름으로도 제공 — 기존/신규 서비스 둘 다 사용 가능
    @Query("""
           select oi.menuItem.menuItemId,
                  oi.menuItem.name,
                  sum(oi.quantity) as qty,
                  sum(oi.quantity * oi.unitPrice) as amount
             from OrderItem oi
            where oi.order.booth.boothId = :boothId
              and oi.order.createdAt between :start and :end
            group by oi.menuItem.menuItemId, oi.menuItem.name
           """)
    List<Object[]> aggregateMenuBetween(@Param("boothId") Long boothId,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    // 특정 부스의 특정 메뉴 총 판매 수량 (통계 API용)
    @Query("""
           select coalesce(sum(oi.quantity), 0)
             from OrderItem oi
            where oi.order.booth.boothId = :boothId
              and oi.menuItem.menuItemId = :menuItemId
           """)
    long totalQtyByBoothAndMenu(@Param("boothId") Long boothId,
                                @Param("menuItemId") Long menuItemId);
    boolean existsByMenuItem_MenuItemId(Long menuItemId);

    // 여러 주문에 대한 라인아이템을 한 번에 로딩 (menuItem까지 로딩)
    @EntityGraph(attributePaths = {"menuItem"})
    List<OrderItem> findByOrder_OrderIdIn(Collection<Long> orderIds);

    List<OrderItem> findByOrder_OrderIdAndIsFinishedFalse(Long orderId);
    List<OrderItem> findByOrder_OrderIdAndIsFinishedTrue(Long orderId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           update OrderItem oi
              set oi.isFinished = :finished
            where oi.order.orderId = :orderId
              and oi.orderItemId = :itemId
           """)
    int updateFinishedOne(@Param("orderId") Long orderId,
                          @Param("itemId") Long itemId,
                          @Param("finished") boolean finished);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           update OrderItem oi
              set oi.isFinished = :finished
            where oi.order.orderId = :orderId
           """)
    int updateFinishedAllInOrder(@Param("orderId") Long orderId,
                                 @Param("finished") boolean finished);
}
