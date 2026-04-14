package com.lvying.web.dto;

import com.lvying.domain.UserRole;
import java.util.UUID;

/** 登录出参：JWT 与当前用户摘要（前端可缓存用于展示角色与姓名）。 */
public record LoginResponse(
    String accessToken, UserInfo user) {

  public record UserInfo(UUID id, String name, String phone, UserRole role) {}
}
