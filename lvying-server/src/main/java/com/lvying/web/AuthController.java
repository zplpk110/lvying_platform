package com.lvying.web;

import com.lvying.service.AuthService;
import com.lvying.web.dto.LoginRequest;
import com.lvying.web.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
