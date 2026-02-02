package com.callcenter.callcenterwas.config;

import com.callcenter.callcenterwas.common.util.SecurityUtil;
import com.callcenter.callcenterwas.domain.agent.entity.Agent;
import com.callcenter.callcenterwas.domain.agent.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AgentDataInitializer {

    @Bean
    public CommandLineRunner initAgents(AgentRepository agentRepository) {
        return args -> {
            if (agentRepository.count() == 0) {
                String username = "agent001";
                String password = "password";
                String name = "김상담";

                // Hash password using the same utility as the login controller
                String passwordHash = SecurityUtil.sha256(password);

                Agent agent = new Agent(username, passwordHash, name);
                agentRepository.save(agent);
                log.info("[Init] Created default agent: {} / {}", username, password);
            }
        };
    }
}
