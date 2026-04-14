package com.lvying.config;

import com.lvying.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * 安全栈：无 Session、JWT 过滤器、除登录外均需认证；CORS 放行本机与局域网前端开发地址（手机连 Wi‑Fi 调试）。
 *
 * <p>方法级权限使用 {@link org.springframework.security.access.prepost.PreAuthorize}（{@code hasRole} 与业务枚举 {@link
 * com.lvying.domain.UserRole} 对应，Spring 自动加 {@code ROLE_} 前缀）。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;
  private final ObjectMapper objectMapper;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg)
      throws Exception {
    return cfg.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(c -> c.configurationSource(cors()))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/auth/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                    (request, response, authException) -> {
                      response.setStatus(401);
                      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                      response.getWriter()
                          .write(
                              objectMapper.writeValueAsString(
                                  Map.of("code", "UNAUTHORIZED", "message", "请先登录")));
                    }))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public CorsConfigurationSource cors() {
    return request -> {
      String origin = request.getHeader("Origin");
      CorsConfiguration c = new CorsConfiguration();
      c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
      c.setAllowedHeaders(List.of("*"));
      c.setAllowCredentials(true);
      if (origin != null && isDevFrontendOrigin(origin)) {
        c.setAllowedOrigins(List.of(origin));
      }
      return c;
    };
  }

  /** 开发环境：本机与常见局域网网段（手机连同一 Wi‑Fi 访问电脑 IP:5173）。 */
  private static boolean isDevFrontendOrigin(String origin) {
    try {
      var uri = java.net.URI.create(origin);
      if (!"http".equalsIgnoreCase(uri.getScheme())) {
        return false;
      }
      String host = uri.getHost();
      if (host == null) {
        return false;
      }
      return host.equals("localhost")
          || host.equals("127.0.0.1")
          || host.startsWith("192.168.")
          || host.startsWith("10.");
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
