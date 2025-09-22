package com.example.bootheat.repository;

import com.example.bootheat.domain.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Optional<MenuItem> findByBooth_BoothIdAndMenuItemId(Long boothId, Long menuItemId);
    List<MenuItem> findByBooth_BoothIdAndAvailableTrue(Long boothId);
    // 관리자용(기존 유지): 전체 목록
    List<MenuItem> findByBooth_BoothIdOrderByNameAsc(Long boothId);
    boolean existsByBooth_BoothIdAndName(Long boothId, String name);
    // 고객용: 판매중만 + 이름 정렬
    List<MenuItem> findByBooth_BoothIdAndAvailableTrueOrderByNameAsc(Long boothId);
}