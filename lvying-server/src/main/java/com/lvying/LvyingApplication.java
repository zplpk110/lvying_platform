package com.lvying;

import com.lvying.config.LvyingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(LvyingProperties.class)
public class LvyingApplication {

  public static void main(String[] args) {
    SpringApplication.run(LvyingApplication.class, args);
  }
}
