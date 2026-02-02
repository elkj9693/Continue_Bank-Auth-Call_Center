package com.callcenter.callcenterwas.api.controller;

import com.callcenter.callcenterwas.common.util.SecurityUtil;
import com.callcenter.callcenterwas.domain.agent.entity.Agent;
import com.callcenter.callcenterwas.domain.agent.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AgentAuthController {

    private final AgentRepository agentRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        log.info("[Auth] Login attempt for user: {}", username);

        Optional<Agent> agentOpt = agentRepository.findByUsername(username);

        if (agentOpt.isPresent()) {
            Agent agent = agentOpt.get();
            String hashedPassword = SecurityUtil.sha256(password);

            if (agent.getPasswordHash().equals(hashedPassword)) {
                log.info("[Auth] Login successful for user: {}", username);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "username", agent.getUsername(),
                        "name", agent.getName()));
            }
        }

        log.warn("[Auth] Login failed for user: {}", username);
        return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid credentials"));
    }
}
