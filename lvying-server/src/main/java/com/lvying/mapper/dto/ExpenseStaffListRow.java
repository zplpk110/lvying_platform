package com.lvying.mapper.dto;

import com.lvying.domain.ExpenseApprovalStatus;
import com.lvying.domain.ExpenseCategory;
import com.lvying.domain.ExpensePayStatus;
import com.lvying.domain.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

/** 业务员「我的垫付」列表行。 */
@Data
public class ExpenseStaffListRow {
  private UUID id;
  private LocalDateTime createdAt;
  private String tourCode;
  private String tourName;
  private ExpenseCategory category;
  private PaymentMethod paymentMethod;
  private BigDecimal amount;
  private ExpenseApprovalStatus approvalStatus;
  private ExpensePayStatus payStatus;
}
