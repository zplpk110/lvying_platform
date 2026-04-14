package com.lvying.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

/**
 * 支出 / 报销单行（表 {@code expenses}），对应 PRD「记一笔支出」。
 *
 * <p><b>资金状态（与 PRD 对齐）</b>
 * <ul>
 *   <li><b>已付成本（Paid_Cost）</b>：{@link PaymentMethod#COMPANY_ACCOUNT} 入账即视同已付；或员工垫付且 {@link
 *       ExpensePayStatus#PAID}。
 *   <li><b>待付成本（Pending_Cost）</b>：员工垫付、{@link ExpenseApprovalStatus#APPROVED} 且尚未打款（{@link
 *       ExpensePayStatus#UNPAID}），计入老板「可用余额」扣减项。
 *   <li>未审批的垫付不计入 Pending_Cost，但计入团维度「待报销/成本预估」用于超支判断。
 * </ul>
 */
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
