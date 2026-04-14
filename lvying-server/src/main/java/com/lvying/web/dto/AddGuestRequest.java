package com.lvying.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record AddGuestRequest(
    @NotBlank String name,
    @NotBlank String phoneMasked,
    String phoneRaw,
    BigDecimal balanceDue) {}
