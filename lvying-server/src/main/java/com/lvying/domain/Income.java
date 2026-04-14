package com.lvying.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 实收流水（表 {@code incomes}）。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Income {

  private UUID id;
  private UUID tourId;
  private BigDecimal amount;
  @Builder.Default private IncomeType type = IncomeType.OTHER;
  private String note;
  private LocalDateTime receivedAt;
  private LocalDateTime createdAt;
}
