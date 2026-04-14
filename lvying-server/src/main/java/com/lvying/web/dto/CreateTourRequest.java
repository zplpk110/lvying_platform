package com.lvying.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** 新建团期请求体；红线可显式传或按毛利率/默认75% 成本上限推算。 */
public record CreateTourRequest(
    @NotBlank String tourCode,
    @NotBlank String name,
    @NotNull LocalDate departureDate,
    @Min(0) int guestCount,
    @NotNull @Min(0) BigDecimal pricePerGuest,
    BigDecimal budgetRedline,
    BigDecimal grossMarginPct,
    UUID salesUserId,
    UUID copyFromTourId) {}
