// src/main/java/com/example/bootheat/dto/OrderItemBrief.java
package com.example.bootheat.dto;

public record OrderItemBrief(
        Long menuItemId,
        String name,
        Integer unitPrice,
        Integer quantity,
        Long lineAmount,
        Boolean isFinished   // 추가됨
) {}
