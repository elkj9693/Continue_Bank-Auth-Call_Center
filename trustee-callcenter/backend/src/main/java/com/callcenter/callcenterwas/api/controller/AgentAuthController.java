package com.callcenter.callcenterwas.api.controller;

import com.callcenter.callcenterwas.domain.agent.entity.Agent;
import com.callcenter.callcenterwas.domain.agent.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AgentAuthController {

    private final AgentRepository agentRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username") != null ? request.get("username").trim() : "";
        String password = request.get("password") != null ? request.get("password").trim() : "";

        log.info("[Auth] Login attempt for user: {}", username);

        Optional<Agent> agentOpt = agentRepository.findByUsername(username);

        if (agentOpt.isPresent()) {
            Agent agent = agentOpt.get();
            boolean matched = passwordEncoder.matches(password, agent.getPasswordHash());
            log.info("[Auth-DEBUG] Agent found: {}. Password match: {}", username, matched);

            if (matched) {
                log.info("[Auth] Login successful for user: {}", username);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "username", agent.getUsername(),
                        "name", agent.getName()));
            }
        } else {
            log.warn("[Auth-DEBUG] Agent NOT FOUND in database: {}", username);
        }

        log.warn("[Auth] Login failed for user: {}", username);
        return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid credentials"));
    }
}
