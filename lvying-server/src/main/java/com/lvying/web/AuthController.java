package com.lvying.web;

import com.lvying.service.AuthService;
import com.lvying.web.dto.LoginRequest;
import com.lvying.web.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 认证：登录获取 JWT，无状态接口不建 Session。 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest req) {
    return authService.login(req);
  }
}
