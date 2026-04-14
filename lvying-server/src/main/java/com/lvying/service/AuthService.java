package com.lvying.service;

import com.lvying.config.JwtService;
import com.lvying.security.AppUserDetails;
import com.lvying.web.dto.LoginRequest;
import com.lvying.web.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * 登录：Spring Security 校验手机号+密码后签发 JWT；前端请求头使用标准 {@code Authorization: Bearer} 携带令牌访问受保护接口。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  /** 手机号登录，返回 accessToken 与基础用户信息。 */
  public LoginResponse login(LoginRequest req) {
    Authentication auth =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.phone(), req.password()));
    AppUserDetails u = (AppUserDetails) auth.getPrincipal();
    String token = jwtService.createToken(u.getId(), u.getPhone(), u.getRole());
    var info = new LoginResponse.UserInfo(u.getId(), u.getName(), u.getPhone(), u.getRole());
    return new LoginResponse(token, info);
  }
}
