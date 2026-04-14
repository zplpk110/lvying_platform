package com.lvying.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

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
