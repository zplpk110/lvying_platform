package com.lvying;

import com.lvying.config.LvyingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 旅盈旅行社轻量管理系统 —— Spring Boot 启动入口。
 *
 * <p>绑定配置前缀 {@code lvying.*}（见 {@link LvyingProperties}），启用 JPA 自动建表/更新（开发环境）。
 */
@SpringBootApplication
@EnableConfigurationProperties(LvyingProperties.class)
public class LvyingApplication {

  public static void main(String[] args) {
    SpringApplication.run(LvyingApplication.class, args);
  }
}
