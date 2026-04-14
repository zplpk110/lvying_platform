package com.lvying.config;

import com.lvying.util.UuidStrings;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

/** 路径参数、请求参数中的 UUID 支持 32 位无连字符或标准格式。 */
@Configuration
public class WebMvcConfig {

  @Bean
  public Converter<String, UUID> stringToUuidConverter() {
    return new Converter<>() {
      @Override
      public UUID convert(@NonNull String source) {
        return UuidStrings.parseLenient(source);
      }
    };
  }
}
