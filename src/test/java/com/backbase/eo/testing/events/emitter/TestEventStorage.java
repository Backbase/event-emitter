package com.backbase.eo.testing.events.emitter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class TestEventStorage {

    private static final Map<String, Message<Map<String, Object>>> EVENTS_STORAGE = new HashMap<>();

    public void addEvent(String correlationId, Message<Map<String, Object>> data) {
        EVENTS_STORAGE.put(correlationId, data);
    }

    public Map<String, Message<Map<String, Object>>> getEvents() {
        return EVENTS_STORAGE;
    }

}
