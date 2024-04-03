/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.backbase.eo.testing.events;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

// @checkstyle:off
@SpringBootApplication
public class EventEmitter {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(EventEmitter.class, args);
    }

    @Bean(name = {"objectMapper"})
    @Primary
    ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper myObjectMapper = builder
            .serializationInclusion(NON_NULL)
            .failOnEmptyBeans(false)
            .failOnUnknownProperties(false)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
        /*
         * Why is it disabled?
         * BigDecimal fields in event definitions are serialized by com.backbase.buildingblocks.backend.api.BigDecimalJsonSerializer
         * which causes "java.lang.NullPointerException: Cannot invoke "Object.getClass()" because "result" is null"
         * while trying to send messages with Spring Cloud Stream (SCS) because we deserialize requests coming from APIs
         * and then serialize again while sending via SCS but SCS is using different serializers which create issues.
         **/
        myObjectMapper.disable(MapperFeature.USE_ANNOTATIONS);

        return myObjectMapper;
    }

}
// @checkstyle:on
