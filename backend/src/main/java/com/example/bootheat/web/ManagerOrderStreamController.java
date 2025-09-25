package com.example.bootheat.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerOrderStreamController {

    // boothId별 emitter 관리
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 매니저 주문 이벤트 구독 (SSE)
     * GET /api/manager/orders/stream?boothId=5
     */
    @GetMapping("/orders/stream")
    public SseEmitter streamOrders(@RequestParam Long boothId) {
        SseEmitter emitter = new SseEmitter(60L * 1000 * 5); // 5분 타임아웃
        emitters.put(boothId, emitter);

        emitter.onCompletion(() -> emitters.remove(boothId));
        emitter.onTimeout(() -> emitters.remove(boothId));
        emitter.onError((ex) -> emitters.remove(boothId));

        // 연결 직후 더미 이벤트 1건 보내기 (연결 확인용)
        try {
            emitter.send(SseEmitter.event()
                    .name("init")
                    .data("connected boothId=" + boothId));
        } catch (IOException e) {
            emitters.remove(boothId);
        }

        return emitter;
    }

    /**
     * 주문 상태 변경 시 이벤트 발송 (서비스에서 호출)
     */
    public void sendOrderUpdate(Long boothId, String message) {
        SseEmitter emitter = emitters.get(boothId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("order-update")
                        .data(message));
            } catch (IOException e) {
                emitters.remove(boothId);
            }
        }
    }
}
