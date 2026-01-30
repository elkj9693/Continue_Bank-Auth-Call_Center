package com.entrusting.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            Map<String, Object> dotenvProps = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                dotenvProps.put(entry.getKey(), entry.getValue());
            });

            if (!dotenvProps.isEmpty()) {
                environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", dotenvProps));
            }
        } catch (Exception e) {
            // Ignore errors if .env is not present or readable
        }
    }
}
