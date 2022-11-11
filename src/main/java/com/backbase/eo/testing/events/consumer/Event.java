package com.backbase.eo.testing.events.consumer;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Event {
    String correlationId;
    String channel;
    String destination;
    Map<String, Object> payload;
}
