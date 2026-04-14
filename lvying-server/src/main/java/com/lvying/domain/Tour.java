package com.lvying.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

/**
 * 团期（表 {@code tours}），财务控制中心的核心聚合根。
 *
 * <p>应收由 {@code guestCount × pricePerGuest} 推导；{@code budgetRedline} 为成本预算红线，用于超支拦截与毛利预警。
 * 销售负责人、提成比例用于结算展示。
 */
@Entity
@Table(name = "tours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tour {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, length = 32)
  private String tourCode;

  @Column(nullable = false, length = 128)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  @Builder.Default
  private TourStatus status = TourStatus.IN_PROGRESS;

  @Column(nullable = false)
  private LocalDate departureDate;

  @Column(nullable = false)
  @Builder.Default
  private int guestCount = 0;

  @Column(nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal pricePerGuest = BigDecimal.ZERO;

  /** 成本预算红线（元），超过则触发超支校验（老板可二次确认）。 */
  @Column(nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal budgetRedline = BigDecimal.ZERO;

  /** 期望毛利率（百分比数值，如 25 表示 25%），新建团时可反算红线。 */
  @Column(nullable = false, precision = 6, scale = 2)
  @Builder.Default
  private BigDecimal grossMarginPct = new BigDecimal("25");

  /** 负责销售/导游，用于催收通知与报销聚合展示。 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sales_user_id")
  private User salesUser;

  /** 净利润提成比例（百分比数值，如 15 表示 15%）。 */
  @Column(nullable = false, precision = 6, scale = 2)
  @Builder.Default
  private BigDecimal commissionRate = new BigDecimal("15");

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Income> incomes = new ArrayList<>();

  @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Expense> expenses = new ArrayList<>();

  @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<TourGuest> guests = new ArrayList<>();

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }
}
