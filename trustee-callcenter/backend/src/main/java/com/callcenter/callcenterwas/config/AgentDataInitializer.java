package com.callcenter.callcenterwas.config;

import com.callcenter.callcenterwas.domain.agent.entity.Agent;
import com.callcenter.callcenterwas.domain.agent.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AgentDataInitializer {

    private final BCryptPasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAgents(AgentRepository agentRepository) {
        return args -> {
            String username = "agent001";
            String password = "password";
            String name = "김상담";

            log.info("[Init-DEBUG] Checking for default agent: {}", username);

            agentRepository.findByUsername(username).ifPresentOrElse(
                    agent -> {
                        log.info("[Init-DEBUG] Agent exists, updating password hash to BCrypt.");
                        agent.setPasswordHash(passwordEncoder.encode(password));
                        agentRepository.save(agent);
                    },
                    () -> {
                        log.info("[Init-DEBUG] Agent not found, creating new agent with BCrypt.");
                        String passwordHash = passwordEncoder.encode(password);
                        Agent agent = new Agent(username, passwordHash, name);
                        agentRepository.save(agent);
                    });

            log.info("[Init] Default agent synchronization complete.");
        };
    }
}
