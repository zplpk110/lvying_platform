package com.lvying.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 登录入参：手机号 + 密码（BCrypt 校验）。 */
public record LoginRequest(
    @NotBlank String phone, @NotBlank @Size(min = 6) String password) {}
