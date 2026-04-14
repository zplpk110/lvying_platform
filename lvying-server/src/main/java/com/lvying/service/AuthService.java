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

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

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
