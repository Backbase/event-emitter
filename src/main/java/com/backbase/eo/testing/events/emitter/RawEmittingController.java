package com.backbase.eo.testing.events.emitter;

import com.backbase.buildingblocks.backend.communication.context.OriginatorContext;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.scs.EventMessageProcessor;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@link RawEmittingController}
 * <br>
 * {@code com.backbase.eo.testing.events.emitter.RawEmittingController}
 * <br>
 *
 * @author Jaco Botha
 * @since 02 December 2022
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class RawEmittingController {

    @Autowired
    private StreamBridge bridge;

    @Autowired(required = false)
    private List<EventMessageProcessor> eventMessageProcessors = Collections.emptyList();

    @PostMapping(
        path = "/events/raw",
        consumes = {MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<Void> emitEvent(@RequestBody RawEventPayload payload) {

        OriginatorContext originatorContext = new OriginatorContext();
        originatorContext.setRequestUuid(payload.getRequestId());
        originatorContext.setCreationTime(payload.getCreationTime());

        EnvelopedEvent envelopedEvent = new EnvelopedEvent();
        envelopedEvent.setOriginatorContext(originatorContext);
        envelopedEvent.setEvent(payload.getBody());

        MessageBuilder eventMessageBuilder = MessageBuilder.withPayload(payload.getBody()).setHeader("bbEventType", payload.getEventType());
        this.eventMessageProcessors.forEach((processor) -> {
            processor.prepareEventMessage(eventMessageBuilder, envelopedEvent);
        });
        Message message = eventMessageBuilder.build();
        log.debug("Event emitted: {}", message.getPayload());

        this.bridge.send(payload.getDestination(), message);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Data
    static class RawEventPayload {

        private String destination;
        private String eventType;
        private String requestId = UUID.randomUUID().toString();
        private long creationTime = Instant.EPOCH.toEpochMilli();
        private Map<String, Object> body;
    }
}
