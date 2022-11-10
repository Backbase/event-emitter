package com.backbase.eo.testing.events.consumer;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunicationEventResolver {

    private final EventStorage eventStorage;

    @Bean
    public Consumer<Message<Map<String, Object>>> resolveCommunicationServiceEvent() {
        return eventMessage -> {
            var correlationId = Optional.ofNullable((String) eventMessage.getHeaders().get("bbRequestUUID")).orElse(UUID.randomUUID().toString());
            eventStorage.addEvent(correlationId, eventMessage);
        };
    }

}
