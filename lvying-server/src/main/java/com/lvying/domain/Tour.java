package com.lvying.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

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

  @Column(nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal budgetRedline = BigDecimal.ZERO;

  @Column(nullable = false, precision = 6, scale = 2)
  @Builder.Default
  private BigDecimal grossMarginPct = new BigDecimal("25");

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sales_user_id")
  private User salesUser;

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
