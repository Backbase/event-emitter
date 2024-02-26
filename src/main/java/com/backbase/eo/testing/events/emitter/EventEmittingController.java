package com.backbase.eo.testing.events.emitter;

import com.backbase.buildingblocks.backend.communication.context.OriginatorContext;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.Nullable;

import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EventEmittingController {

    private final EventBus eventBus;
    private final ObjectMapper mapper;

    private static final int BATCH_SIZE = 10000;
    private static final int USERS_COUNT = 20;
    private static final Random random = new Random();

    @PostMapping(
        path="/events/{eventId}",
        consumes = {MediaType.APPLICATION_JSON_VALUE}
    )
    public void emitEvent(
        @PathVariable("eventId") String eventId,
        @Nullable @RequestBody String body,
        @RequestHeader(value = "bbRequestUUID", required = false) String bbRequestUUID) {

        EnvelopedEvent envelopedEvent = buildEventPayload(eventId, body, bbRequestUUID);
        eventBus.emitEvent(envelopedEvent);
    }

    @PostMapping(
        path="/mass-emit/{eventId}",
        consumes = {MediaType.APPLICATION_JSON_VALUE}
    )
    public void emitMany(@PathVariable("eventId") String eventId, @Nullable @RequestBody String body,
        @RequestParam(value = "countEvents", defaultValue = "1000") Integer countEvents,
        @RequestHeader(value = "bbRequestUUID", required = false) String bbRequestUUID) throws InterruptedException {

        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(USERS_COUNT);
        threadPoolTaskExecutor.setQueueCapacity(countEvents);
        threadPoolTaskExecutor.initialize();

        EnvelopedEvent envelopedEvent = buildEventPayload(eventId, body, bbRequestUUID);

        long start = System.currentTimeMillis();
        for (int i = 0; i < BATCH_SIZE; i++) {
            threadPoolTaskExecutor.submit(() -> eventBus.emitEvent(envelopedEvent));
        }
        while (threadPoolTaskExecutor.getThreadPoolExecutor().getActiveCount() != 0) {
            System.err.println("Still working...");
            TimeUnit.SECONDS.sleep(2);
        }
        System.err.println(String.format("Done in %s ms", System.currentTimeMillis() - start));

    }

    private EnvelopedEvent buildEventPayload(String eventId, String body, String requestUuid) {
        Reflections reflections = new Reflections("com.backbase");
        Set<Class<? extends Event>> availableEvents = reflections.getSubTypesOf(Event.class);

        OriginatorContext originatorContext = new OriginatorContext();
        originatorContext.setRequestUuid(requestUuid);
        originatorContext.setCreationTime(Instant.EPOCH.toEpochMilli());

        Object testObject = availableEvents.stream()
            .filter(clazz -> clazz.getName().equalsIgnoreCase(eventId))
            .findFirst()
            .flatMap(clazz -> {
                try {
                    if (body != null) {
                        return Optional.of(mapper.readValue(body, clazz));
                    } else {
                        return Optional.of(createAndFill(clazz));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return Optional.empty();
                }
            }).orElseThrow(() -> new BadRequestException("Unknown event"));

        EnvelopedEvent envelopedEvent = new EnvelopedEvent();
        envelopedEvent.setOriginatorContext(originatorContext);
        envelopedEvent.setEvent(testObject);

        return envelopedEvent;
    }


    public <T> T createAndFill(Class<T> clazz) throws Exception {
        T instance = clazz.newInstance();
        for(Field field: clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = getRandomValueForField(field);
            field.set(instance, value);
        }
        return instance;
    }

    private Object getRandomValueForField(Field field) throws Exception {
        Class<?> type = field.getType();

        if(type.isEnum()) {
            Object[] enumValues = type.getEnumConstants();
            return enumValues[random.nextInt(enumValues.length)];
        } else if(type.equals(Integer.TYPE) || type.equals(Integer.class)) {
            return random.nextInt();
        } else if(type.equals(Long.TYPE) || type.equals(Long.class)) {
            return random.nextLong();
        } else if(type.equals(Double.TYPE) || type.equals(Double.class)) {
            return random.nextDouble();
        } else if(type.equals(Float.TYPE) || type.equals(Float.class)) {
            return random.nextFloat();
        } else if(type.equals(String.class)) {
            return UUID.randomUUID().toString();
        } else if(type.equals(BigInteger.class)){
            return BigInteger.valueOf(random.nextInt());
        } else if(type.equals(Boolean.class)){
            return random.nextBoolean();
        } else if(type.equals(BigDecimal.class)){
            return BigDecimal.valueOf(random.nextInt());
        } else if(type.equals(List.class)){
            return new ArrayList<>();
        } else if(type.equals(Map.class)){
            return new HashMap<>();
        }
        return createAndFill(type);
    }
}
