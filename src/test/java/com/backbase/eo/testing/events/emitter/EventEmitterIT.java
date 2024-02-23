package com.backbase.eo.testing.events.emitter;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import com.backbase.eo.testing.events.EventEmitter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.testcontainers.containers.output.Slf4jLogConsumer;

/**
 * {@link EventEmitterIT}
 * <br>
 * {@code com.backbase.eo.testing.events.EventEmitterTest}
 * <br>
 *
 * @author Jaco Botha
 * @since 16 November 2022
 */
@AutoConfigureMockMvc
@ActiveProfiles({"default", "it"})
@SpringBootTest(classes = {EventEmitter.class, TestEventConsumerConfiguration.class})
@ContextConfiguration(classes = {EventEmitter.class, TestEventConsumerConfiguration.class}, initializers = {EventEmitterIT.Initializer.class})
@Slf4j
class EventEmitterIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestEventStorage eventStorage;

    private static final String EVENT_DESTINATION = "com.backbase.dbs.messages.pandp.event.spec.v4.MessageReceivedEvent";

    @ClassRule
    public static DockerComposeContainer environment = new DockerComposeContainer(
        new File("src/test/resources/docker-compose.yml"))
        .withExposedService("message-broker", 61616)
        .withLogConsumer("message-broker", new Slf4jLogConsumer(log));

    @BeforeAll
    public static void envSetup() {
        System.setProperty("SIG_SECRET_KEY", "JWTSecretKeyDontUseInProduction!");
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            environment.start();

            String activeMqHost = environment.getServiceHost("message-broker", 61616);
            Integer activeMqPort = environment.getServicePort("message-broker", 61616);

            TestPropertyValues.of(
                    "spring.activemq.broker-url=tcp://%s:%s".formatted(activeMqHost, activeMqPort),
                    "spring.event-emitter.topic-names=" + EVENT_DESTINATION,
                    "spring.cloud.stream.bindings.consumeTestEvent-in-0.group=event-emitter",
                    "spring.cloud.stream.bindings.consumeTestEvent-in-0.destination=" + EVENT_DESTINATION,
                    "spring.cloud.function.definition=consumeTestEvent",
                    "backbase.event-emitter.custom-header-pairs[0].http=x-lob",
                    "backbase.event-emitter.custom-header-pairs[0].event=bbLineOfBusiness",
                    "backbase.event-emitter.custom-header-pairs[1].http=customerCategory",
                    "backbase.event-emitter.custom-header-pairs[1].event=customerEventCategory")
                .applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Test
    @DisplayName("Test that an event can be emitted")
    void emitEvent() throws Exception {
        String uuid = UUID.randomUUID().toString();

        mockMvc.perform(post("/events/" + EVENT_DESTINATION)
            .header("bbRequestUUID", uuid)
            .contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "subject":"postman test",
                    "body":"dGVzdCB0ZXN0",
                    "topic":"4fe20af6-811a-4be3-aa49-6b28d3e6b0f1",
                    "recipient":"19aa3423-4bc3-4624-bbbf-75064a441b44",
                    "important":false,
                    "deletable":true,
                    "read-only":false
                }
                """)).andDo(print()).andExpect(status().isOk());

        await().until(() -> eventStorage.getEvents().containsKey(uuid));
        assertThat(eventStorage.getEvents().keySet()).contains(uuid);
        assertThat(eventStorage.getEvents().get(uuid).getHeaders().keySet()).contains("bbEventType");
        assertThat(eventStorage.getEvents().get(uuid).getHeaders().get("bbEventType")).isEqualTo(EVENT_DESTINATION);
    }

    @ParameterizedTest
    @MethodSource("customHeadersData")
    @DisplayName("Test that a raw event with custom headers can be emitted")
    void emitRawEventWithCustomHeader(Map<String, String> customHeaders, Map<String, String> expectedCustomHeaders) throws Exception {
        String uuid = UUID.randomUUID().toString();

        RawEmittingController.RawEventPayload rawEventPayload = new RawEmittingController.RawEventPayload();
        rawEventPayload.setEventType(EVENT_DESTINATION);
        rawEventPayload.setBody(Map.of(
            "subject", "postman test",
            "body", "dGVzdCB0ZXN0",
            "topic", "4fe20af6-811a-4be3-aa49-6b28d3e6b0f1",
            "recipient", "19aa3423-4bc3-4624-bbbf-75064a441b44",
            "important", "false",
            "deletable", "true",
            "read-only", "false"
        ));
        rawEventPayload.setDestination(EVENT_DESTINATION);
        rawEventPayload.setRequestId(uuid);
        rawEventPayload.setCreationTime(Instant.now().toEpochMilli());

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(rawEventPayload);

        HttpHeaders headers = new HttpHeaders();
        headers.setAll(customHeaders);

        mockMvc.perform(post("/events/raw")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(json))
            .andDo(print()).andExpect(status().isCreated());

        await().until(() -> eventStorage.getEvents().containsKey(uuid));
        assertThat(eventStorage.getEvents().get(uuid).getHeaders().keySet()).contains("bbEventType");
        assertThat(eventStorage.getEvents().get(uuid).getHeaders().get("bbEventType")).isEqualTo(EVENT_DESTINATION);
        assertThat(eventStorage.getEvents().keySet()).contains(uuid);
        ofNullable(expectedCustomHeaders).orElse(emptyMap()).forEach((expectedKey, expectedValue) -> {
            assertThat(eventStorage.getEvents().get(uuid).getHeaders().keySet()).contains(expectedKey);
            assertThat(eventStorage.getEvents().get(uuid).getHeaders().get(expectedKey)).isEqualTo(expectedValue);
        });
        assertThat(eventStorage.getEvents().get(uuid).getHeaders().keySet()).doesNotContain("anotherHeader");
        assertThat(eventStorage.getEvents().get(uuid).getHeaders().keySet()).doesNotContain("X-LOB");
        assertThat(eventStorage.getEvents().get(uuid).getHeaders().keySet()).doesNotContain("customerCategory");
    }

    public static Object[][] customHeadersData() {
        return new Object[][]{
            {Collections.singletonMap("X-LOB", "RETAIL"),
                Collections.singletonMap("bbLineOfBusiness", "RETAIL")},
            {Map.of("customerCategory", "RETAIL", "X-LOB", "CUSTOMER"),
                Map.of("customerEventCategory", "RETAIL", "bbLineOfBusiness", "CUSTOMER")},
            {Map.of("customerCategory", "RETAIL", "X-LOB", "CUSTOMER", "anotherHeader", "anotherValue"),
                Map.of("customerEventCategory", "RETAIL", "bbLineOfBusiness", "CUSTOMER")}
        };
    }
}