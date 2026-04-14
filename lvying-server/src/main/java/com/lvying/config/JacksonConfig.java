package com.lvying.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.lvying.util.UuidStrings;
import java.io.IOException;
import java.util.UUID;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** REST JSON 中 UUID 序列化为 32 位无连字符；反序列化兼容带/不带连字符。 */
@Configuration
public class JacksonConfig {

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer uuidCompactInJson() {
    return builder ->
        builder
            .serializerByType(
                UUID.class,
                new JsonSerializer<UUID>() {
                  @Override
                  public void serialize(
                      UUID value, JsonGenerator gen, SerializerProvider serializers)
                      throws IOException {
                    gen.writeString(UuidStrings.compact(value));
                  }
                })
            .deserializerByType(
                UUID.class,
                new JsonDeserializer<UUID>() {
                  @Override
                  public UUID deserialize(JsonParser p, DeserializationContext ctxt)
                      throws IOException {
                    String v = p.getValueAsString();
                    if (v == null) {
                      return null;
                    }
                    return UuidStrings.parseLenient(v);
                  }
                });
  }
}
