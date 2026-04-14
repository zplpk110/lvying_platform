package com.lvying.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支出 / 报销（表 {@code expenses}）。外键：{@link #tourId}、{@link #staffUserId}、{@link #approvedById}。
 *
 * <p>资金口径说明见原 PRD 注释（已付 / 待付 / 审批状态）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

  private UUID id;
  private UUID tourId;
  private BigDecimal amount;
  private ExpenseCategory category;
  private PaymentMethod paymentMethod;
  private UUID staffUserId;
  private String receiptImageUrl;
  private String note;
  @Builder.Default private ExpenseApprovalStatus approvalStatus = ExpenseApprovalStatus.PENDING;
  @Builder.Default private ExpensePayStatus payStatus = ExpensePayStatus.UNPAID;
  private UUID approvedById;
  private LocalDateTime approvedAt;
  private String batchPayRef;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
