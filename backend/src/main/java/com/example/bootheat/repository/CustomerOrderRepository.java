package com.example.bootheat.repository;

import com.example.bootheat.domain.CustomerOrder;
import com.example.bootheat.dto.StatsTotals;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    // 테이블 최근 주문 10건 (QueryService.getTableContext)
    List<CustomerOrder> findTop10ByTable_TableIdOrderByCreatedAtDesc(Long tableId);

    // 특정 방문(visit)의 주문들
    List<CustomerOrder> findByVisit_VisitIdOrderByCreatedAtDesc(Long visitId);

    // ⬇️ limit 적용용
    Page<CustomerOrder> findByVisit_VisitIdOrderByCreatedAtDesc(Long visitId, Pageable pageable);

    // 특정 부스-테이블 전체 주문
    List<CustomerOrder> findByBooth_BoothIdAndTable_TableIdOrderByCreatedAtDesc(Long boothId, Long tableId);

    // 일자 범위 내 총 주문수 / 총액 (StatsService.todayStats에서 사용)
    @Query("""
           select count(o) as cnt,
                  coalesce(sum(o.totalAmount), 0) as amount
             from CustomerOrder o
            where o.booth.boothId = :boothId
              and o.createdAt between :start and :end
           """)
    Object[] sumToday(@Param("boothId") Long boothId,
                      @Param("start") LocalDateTime start,
                      @Param("end") LocalDateTime end);

    // 시간대별 주문 수 (피크아워 계산용)
    @Query("""
           select function('hour', o.createdAt) as hr,
                  count(o) as cnt
             from CustomerOrder o
            where o.booth.boothId = :boothId
              and o.createdAt between :start and :end
            group by function('hour', o.createdAt)
            order by hr
           """)
    List<Object[]> hourlyCounts(@Param("boothId") Long boothId,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    // (확장) 이름만 다른 범용 버전 — 신규 StatsService에서도 사용 가능
// repository/CustomerOrderRepository.java
// repository/CustomerOrderRepository.java
    @Query("""
       select new com.example.bootheat.dto.StatsTotals(
           count(o),                       
           coalesce(sum(o.totalAmount),0)  
       )
       from CustomerOrder o
       where o.booth.boothId = :boothId
         and o.createdAt >= :start
         and o.createdAt <  :end
       """)
    StatsTotals sumBetween(@Param("boothId") Long boothId,
                           @Param("start") java.time.LocalDateTime start,
                           @Param("end")   java.time.LocalDateTime end);


    @Query("""
       select function('hour', o.createdAt) as hr,
              count(o) as cnt
       from CustomerOrder o
       where o.booth.boothId = :boothId
         and o.createdAt >= :start
         and o.createdAt <  :end
       group by function('hour', o.createdAt)
       order by hr
       """)
    List<Object[]> hourlyCountsBetween(@Param("boothId") Long boothId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end")   LocalDateTime end);
    // 모든 부스: 날짜 구간 내 주문 (boothId, createdAt 정렬)
    List<CustomerOrder> findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByBooth_BoothIdAscCreatedAtAsc(
            LocalDateTime start, LocalDateTime end
    );


    // 모든 부스: 날짜 구간 [start, end) 총 주문수/총매출
    @Query("""
       select new com.example.bootheat.dto.StatsTotals(
           count(o),                       
           coalesce(sum(o.totalAmount),0)  
       )
       from CustomerOrder o
       where o.createdAt >= :start
         and o.createdAt <  :end
       """)
    StatsTotals sumAllBetween(@Param("start") java.time.LocalDateTime start,
                              @Param("end")   java.time.LocalDateTime end);
}