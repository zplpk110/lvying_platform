package com.lvying.config;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "lvying")
public class LvyingProperties {

  private Jwt jwt = new Jwt();
  private BigDecimal reservedSafeCash = BigDecimal.ZERO;

  @Getter
  @Setter
  public static class Jwt {
    private String secret = "";
    private long expirationMs = 604800000L;
  }
}
