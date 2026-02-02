package com.entrusting.backend.ars.controller;

import com.entrusting.backend.ars.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 카드 관련 엔드포인트
 */
@RestController
@RequestMapping("/issuer/card")
@RequiredArgsConstructor
public class CardController {

    private final CardRepository cardRepository;

    /**
     * 카드 분실 신고
     */
    @PostMapping("/loss")
    public Map<String, Object> reportLoss(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> cardRefs = (List<String>) request.get("selectedCardRefs");

        for (String cardRef : cardRefs) {
            cardRepository.findByCardRef(cardRef).ifPresent(card -> {
                card.setStatus("LOST");
                cardRepository.save(card);
            });
        }

        String lossCaseId = "LOSS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return Map.of(
                "success", true,
                "lossCaseId", lossCaseId,
                "processedCount", cardRefs.size(),
                "timestamp", LocalDateTime.now().toString());
    }

}
