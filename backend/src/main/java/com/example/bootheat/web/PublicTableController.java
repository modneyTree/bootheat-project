package com.example.bootheat.web;


import com.example.bootheat.dto.OrderDetailResponse;
import com.example.bootheat.dto.TableDto;
import com.example.bootheat.dto.TableListItem;
import com.example.bootheat.service.QueryService;
import com.example.bootheat.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booths")
@RequiredArgsConstructor
class PublicTableController {
    private final TableService tableService;
    private final QueryService queryService;

    // GET /api/booths/{boothId}/tables
    @GetMapping("/{boothId}/tables")
    public List<TableListItem> list(@PathVariable Long boothId) {
        return tableService.listWithVisitStatus(boothId);
    }

    // GET /api/booths/{boothId}/tables/{tableId}/visits/latest/orders
    @GetMapping("/{boothId}/tables/{tableId}/visits/latest/orders")
    public List<OrderDetailResponse> getLatestVisitOrders(
            @PathVariable Long boothId,
            @PathVariable Long tableId,
            @RequestParam(defaultValue = "10") int limit) {
        return queryService.findLatestVisitOrders(boothId, tableId, limit);
    }

    // GET /api/booths/{boothId}/tables/{tableId}/visits/{visitId}/orders
    @GetMapping("/{boothId}/tables/{tableId}/visits/{visitId}/orders")
    public List<OrderDetailResponse> getOrdersByVisit(
            @PathVariable Long boothId,
            @PathVariable Long tableId,
            @PathVariable Long visitId,
            @RequestParam(defaultValue = "10") int limit) {
        return queryService.findOrdersByVisit(boothId, tableId, visitId, limit);
    }
}