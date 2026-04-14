package com.lvying.web.dto;

import com.lvying.domain.ExpenseCategory;
import com.lvying.domain.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateExpenseRequest(
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotNull ExpenseCategory category,
    @NotNull PaymentMethod paymentMethod,
    UUID staffUserId,
    String receiptImageUrl,
    String note,
    Boolean bossConfirmed) {}
