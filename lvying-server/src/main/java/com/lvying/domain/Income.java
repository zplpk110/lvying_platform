package com.lvying.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

/**
 * 实收流水（表 {@code incomes}），对应 PRD 的 Actual_Income：老板侧已确认到账的团款。
 *
 * <p>MVP 中创建记录即视为已入账；{@link IncomeType} 区分定金、尾款等，便于团详情「收银台」分项展示。
 */
@Entity
@Table(name = "incomes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Income {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tour_id", nullable = false)
  private Tour tour;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  @Builder.Default
  private IncomeType type = IncomeType.OTHER;

  @Column(length = 255)
  private String note;

  @Column(nullable = false)
  @Builder.Default
  private Instant receivedAt = Instant.now();

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    createdAt = Instant.now();
    if (receivedAt == null) receivedAt = createdAt;
  }
}
