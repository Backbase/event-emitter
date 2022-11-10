package com.backbase.eo.testing.events.consumer;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EventConsumerController {

    private final EventStorage eventStorage;

    @GetMapping(
        path = "/events"
    )
    public List<Event> getAllEvents() {
        return eventStorage.getEvents();
    }

    @GetMapping(
        path = "/events/{correlationId}"
    )
    public Event getEvent(@PathVariable("correlationId") String correlationId) {
        return eventStorage.getEvents().stream().filter(event -> Objects.equals(event.getCorrelationId(), correlationId)).findFirst().orElse(null);
    }

    @DeleteMapping(
        path = "/events"
    )
    public void deleteEvents() {
        eventStorage.deleteAllEvents();
        log.info("Removed all events");
    }

}
