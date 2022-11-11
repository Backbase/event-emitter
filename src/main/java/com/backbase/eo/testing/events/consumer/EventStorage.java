package com.backbase.eo.testing.events.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventStorage {

    private static List<Event> MEMORY_STORAGE = new ArrayList<>();

    public void addEvent(String correlationId, Message<Map<String, Object>> data) {
        var event = buildEvent(correlationId, data);
        log.info("Got event with correlationId: {}, event: '{}'", correlationId, event);
        MEMORY_STORAGE.add(event);
    }

    public List<Event> getEvents() {
        return MEMORY_STORAGE;
    }

    public void deleteAllEvents() {
        MEMORY_STORAGE = new ArrayList<>();
    }

    private static Event buildEvent(String correlationId, Message<Map<String, Object>> data) {
        var headers = data.getHeaders().entrySet().stream()
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        var msgChannel = (String) headers.get("msgChannel");
        var jmsDestination = headers.get("jms_destination");
        var destination = "unknown";
        if (jmsDestination instanceof ActiveMQQueue){
            destination = ((ActiveMQQueue) jmsDestination).getPhysicalName();
        }
        var payload = data.getPayload();
        return new Event(correlationId, msgChannel, destination, payload);
    }

}
