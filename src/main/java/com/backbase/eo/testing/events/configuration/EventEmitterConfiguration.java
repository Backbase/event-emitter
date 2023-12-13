package com.backbase.eo.testing.events.configuration;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Getter
@ConfigurationProperties("backbase.event-emitter")
@EnableConfigurationProperties(value = EventEmitterConfiguration.class)
public class EventEmitterConfiguration {

    private final List<CustomHeaderPairs> customHeaderPairs = new ArrayList<>();

    public record CustomHeaderPairs(
        String http,
        String event) {
    }
}