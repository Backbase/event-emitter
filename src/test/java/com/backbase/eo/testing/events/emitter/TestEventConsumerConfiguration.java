package com.backbase.eo.testing.events.emitter;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.Message;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@TestConfiguration
@RequiredArgsConstructor
public class TestEventConsumerConfiguration {

    private final TestEventStorage eventStorage;

    @Bean
    @Primary
    public Consumer<Message<Map<String, Object>>> consumeTestEvent() {
        return eventMessage -> {
            var correlationId = Optional.ofNullable((String) eventMessage.getHeaders().get("bbRequestUUID")).orElse(UUID.randomUUID().toString());
            eventStorage.addEvent(correlationId, eventMessage);
        };
    }

}
