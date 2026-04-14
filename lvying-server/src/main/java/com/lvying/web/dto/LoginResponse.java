package com.lvying.web.dto;

import com.lvying.domain.UserRole;
import java.util.UUID;

public record LoginResponse(
    String accessToken, UserInfo user) {

  public record UserInfo(UUID id, String name, String phone, UserRole role) {}
}
