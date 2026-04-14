package com.lvying.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 团期（表 {@code tours}）。销售用 {@link #salesUserId} 外键；详情页需销售姓名时再查 {@code users}。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tour {

  private UUID id;
  private String tourCode;
  private String name;
  @Builder.Default private TourStatus status = TourStatus.IN_PROGRESS;
  private LocalDate departureDate;
  @Builder.Default private int guestCount = 0;
  @Builder.Default private BigDecimal pricePerGuest = BigDecimal.ZERO;
  /** 成本预算红线（元）。 */
  @Builder.Default private BigDecimal budgetRedline = BigDecimal.ZERO;
  /** 期望毛利率（%）。 */
  @Builder.Default private BigDecimal grossMarginPct = new BigDecimal("25");
  /** 负责销售用户 id，可空。 */
  private UUID salesUserId;
  /** 净利润提成比例（%）。 */
  @Builder.Default private BigDecimal commissionRate = new BigDecimal("15");
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
