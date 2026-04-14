package com.lvying.mapper.dto;

import com.lvying.domain.ExpenseApprovalStatus;
import com.lvying.domain.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

/** 报销审批聚合：费用 + 团 + 垫付人（一条 SQL）。 */
@Data
public class ExpensePendingRow {
  private UUID id;
  private BigDecimal amount;
  private ExpenseCategory category;
  private String receiptImageUrl;
  private ExpenseApprovalStatus approvalStatus;
  private LocalDateTime createdAt;
  private UUID tourId;
  private String tourCode;
  private String tourName;
  private UUID staffUserId;
  private String staffName;
}
