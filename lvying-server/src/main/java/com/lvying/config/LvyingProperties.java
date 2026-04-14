package com.lvying.config;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 业务配置绑定（{@code application.yml} 前缀 {@code lvying}）。
 *
 * <ul>
 *   <li>{@code lvying.jwt.*}：签名密钥与过期时间
 *   <li>{@code lvying.reserved-safe-cash}：老板可用余额中额外扣减的安全垫（元）
 * </ul>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "lvying")
public class LvyingProperties {

  private Jwt jwt = new Jwt();
  /** 预留安全金，从可用余额中扣除，防止算得太满（默认 0）。 */
  private BigDecimal reservedSafeCash = BigDecimal.ZERO;

  @Getter
  @Setter
  public static class Jwt {
    private String secret = "";
    private long expirationMs = 604800000L;
  }
}
