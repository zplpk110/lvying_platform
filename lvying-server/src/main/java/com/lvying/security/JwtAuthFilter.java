package com.lvying.security;

import com.lvying.config.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 从 {@code Authorization: Bearer} 解析 JWT，校验后把 {@link AppUserDetails} 放入 {@link
 * org.springframework.security.core.context.SecurityContext}。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final CustomUserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      try {
        Claims claims = jwtService.parse(token);
        String phone = claims.get("phone", String.class);
        if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
          AppUserDetails user = (AppUserDetails) userDetailsService.loadUserByUsername(phone);
          UsernamePasswordAuthenticationToken auth =
              new UsernamePasswordAuthenticationToken(
                  user, null, user.getAuthorities());
          auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      } catch (Exception ignored) {
        // invalid token -> anonymous
      }
    }
    filterChain.doFilter(request, response);
  }
}
