package com.lvying.config;

import com.lvying.domain.UserRole;
import com.lvying.util.UuidStrings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 * JWT 签发与解析：subject 为用户 UUID（32 位无连字符），自定义 claims 含手机号与角色，供 {@link
 * com.lvying.security.JwtAuthFilter} 还原登录态。
 */
@Service
public class JwtService {

  private final LvyingProperties props;

  public JwtService(LvyingProperties props) {
    this.props = props;
  }

  private SecretKey key() {
    String s = props.getJwt().getSecret();
    if (s == null || s.length() < 32) {
      s = (s == null ? "" : s) + "0123456789abcdef0123456789abcdef";
      s = s.substring(0, 32);
    }
    return Keys.hmacShaKeyFor(s.getBytes(StandardCharsets.UTF_8));
  }

  /** 生成 HS256 签名 Token，默认有效期见配置 {@code lvying.jwt.expiration-ms}。 */
  public String createToken(UUID userId, String phone, UserRole role) {
    long exp = props.getJwt().getExpirationMs();
    Date now = new Date();
    return Jwts.builder()
        .subject(UuidStrings.compact(userId))
        .claim("phone", phone)
        .claim("role", role.name())
        .issuedAt(now)
        .expiration(new Date(now.getTime() + exp))
        .signWith(key())
        .compact();
  }

  /** 校验签名并解析声明；非法 Token 由调用方捕获。 */
  public Claims parse(String token) {
    return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
  }
}
