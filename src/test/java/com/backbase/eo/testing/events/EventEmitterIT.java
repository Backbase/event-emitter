package com.backbase.eo.testing.events;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.util.UUID;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.DockerComposeContainer;

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
@SpringBootTest(classes = {EventEmitter.class})
@ContextConfiguration(classes = EventEmitter.class, initializers = {EventEmitterIT.Initializer.class})
class EventEmitterIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext context;

    @ClassRule
    public static DockerComposeContainer environment = new DockerComposeContainer(
        new File("src/test/resources/docker-compose.yml"))
        .withExposedService("message-broker", 61616);

    @BeforeAll
    public static void envSetup() {
        System.setProperty("SIG_SECRET_KEY", "JWTSecretKeyDontUseInProduction!");
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            environment.start();

            TestPropertyValues.of(
                    "spring.activemq.broker-url=tcp://%s:%s".formatted(environment.getServiceHost("message-broker", 61616),
                        environment.getServicePort("message-broker", 61616)),
                    "spring.event-emitter.topic-names=com.backbase.dbs.messages.pandp.event.spec.v4.MessageReceivedEvent")
                .applyTo(configurableApplicationContext.getEnvironment());

        }
    }


    @Test
    @DisplayName("Test that an event can be emitted")
    void emitEvent() throws Exception {
        String uuid = UUID.randomUUID().toString();

        mockMvc.perform(post("/events/com.backbase.dbs.messages.pandp.event.spec.v4.MessageReceivedEvent")
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

        System.out.println("");
    }
}