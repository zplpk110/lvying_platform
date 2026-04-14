package com.lvying.web.dto;

import com.lvying.domain.IncomeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** 记一笔团款收入。 */
public record RecordIncomeRequest(
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotNull IncomeType type,
    String note) {}
