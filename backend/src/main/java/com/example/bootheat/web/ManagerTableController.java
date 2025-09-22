package com.example.bootheat.web;

import com.example.bootheat.dto.CloseVisitRequest;
import com.example.bootheat.dto.CreateTableRequest;
import com.example.bootheat.dto.TableContextResponse;
import com.example.bootheat.dto.TableDto;
import com.example.bootheat.service.OrderService;
import com.example.bootheat.service.QueryService;
import com.example.bootheat.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
class ManagerTableController {
    private final OrderService orderService;
    private final QueryService queryService;
    private final TableService tableService; // ⬅︎ 신규 서비스

    // 기존: /api/manager/tables/{boothId}/{tableNo}/close-visit
    @PostMapping("/tables/{tableId}/close-visit")
    public ResponseEntity<Void> closeVisitByTableId(@PathVariable Long tableId,
                                                    @RequestBody(required = false) CloseVisitRequest req) {
        if (req != null && req.tableId() != null && !req.tableId().equals(tableId)) {
            throw new IllegalArgumentException("TABLE_ID_MISMATCH");
        }
        orderService.closeCurrentVisitByTableId(tableId);
        return ResponseEntity.ok().build(); // 200
    }

    // POST /api/manager/booths/{boothId}/tables
    @PostMapping("/booths/{boothId}/tables")
    public ResponseEntity<TableDto> create(@PathVariable Long boothId,
                                           @RequestBody CreateTableRequest req) {
        var res = tableService.create(boothId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res); // 201 Created + JSON 반환
    }

}