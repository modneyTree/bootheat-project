package com.example.bootheat.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="order_item")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="order_item_id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="order_id", nullable=false)
    private CustomerOrder order;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="menu_item_id", nullable=false)
    private MenuItem menuItem;

    @Column(nullable=false) private Integer quantity;
    @Column(nullable=false) private Integer unitPrice;

    // ✅ 추가: 항목 완료 여부
    @Column(nullable = false)
    @Builder.Default
    private Boolean isFinished = false;
}
