package com.lvying.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank String phone, @NotBlank @Size(min = 6) String password) {}
