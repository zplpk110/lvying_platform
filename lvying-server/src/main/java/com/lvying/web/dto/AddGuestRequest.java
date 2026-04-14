package com.lvying.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/** 为团添加游客，用于催收名单与欠费展示。 */
public record AddGuestRequest(
    @NotBlank String name,
    @NotBlank String phoneMasked,
    String phoneRaw,
    BigDecimal balanceDue) {}
