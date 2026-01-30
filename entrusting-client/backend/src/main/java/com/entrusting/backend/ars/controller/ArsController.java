package com.entrusting.backend.ars.controller;

import com.entrusting.backend.ars.service.ArsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ARS 관련 엔드포인트
 */
@RestController
@RequestMapping("/issuer/ars")
@RequiredArgsConstructor
public class ArsController {

    private final ArsService arsService;

    /**
     * ANI로 고객 식별
     */
    @PostMapping("/identify")
    public Map<String, Object> identify(@RequestBody Map<String, String> request) {
        return arsService.identifyCustomer(request.get("phoneNumber"));
    }

    /**
     * 암호화된 PIN 검증 및 카드 목록 조회
     */
    @PostMapping("/verify-pin")
    public Map<String, Object> verifyPin(@RequestBody Map<String, Object> request) {
        return arsService.verifyPinAndGetCards(request);
    }
}
