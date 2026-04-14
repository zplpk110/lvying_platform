package com.lvying.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tour_id", nullable = false)
  private Tour tour;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private ExpenseCategory category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private PaymentMethod paymentMethod;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "staff_user_id")
  private User staffUser;

  @Column(length = 512)
  private String receiptImageUrl;

  @Column(length = 512)
  private String note;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  @Builder.Default
  private ExpenseApprovalStatus approvalStatus = ExpenseApprovalStatus.PENDING;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  @Builder.Default
  private ExpensePayStatus payStatus = ExpensePayStatus.UNPAID;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "approved_by_id")
  private User approvedBy;

  private Instant approvedAt;

  @Column(length = 64)
  private String batchPayRef;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

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
