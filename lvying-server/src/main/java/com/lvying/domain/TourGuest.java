package com.lvying.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

/**
 * 团内游客（表 {@code tour_guests}），用于尾款催收名单与短信话术中的脱敏展示。
 *
 * <p>{@code phoneMasked} 给界面与日志使用；{@code phoneRaw} 可存真实号（注意隐私合规）。{@code balanceDue} 为该游客维度欠尾款，可与团总欠联动。
 */
@Entity
@Table(name = "tour_guests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourGuest {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tour_id", nullable = false)
  private Tour tour;

  @Column(nullable = false, length = 64)
  private String name;

  @Column(nullable = false, length = 32)
  private String phoneMasked;

  @Column(length = 32)
  private String phoneRaw;

  @Column(nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal balanceDue = BigDecimal.ZERO;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    createdAt = Instant.now();
  }
}
