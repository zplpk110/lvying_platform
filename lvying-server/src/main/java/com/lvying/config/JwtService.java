package com.lvying.config;

import com.lvying.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

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

  public String createToken(UUID userId, String phone, UserRole role) {
    long exp = props.getJwt().getExpirationMs();
    Date now = new Date();
    return Jwts.builder()
        .subject(userId.toString())
        .claim("phone", phone)
        .claim("role", role.name())
        .issuedAt(now)
        .expiration(new Date(now.getTime() + exp))
        .signWith(key())
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
  }
}
