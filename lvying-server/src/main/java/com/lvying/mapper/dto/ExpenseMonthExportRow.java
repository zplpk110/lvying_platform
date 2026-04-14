package com.lvying.mapper.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

/** 月结代发：已审未付垫付 + 员工银行卡信息。 */
@Data
public class ExpenseMonthExportRow {
  private UUID id;
  private UUID staffUserId;
  private BigDecimal amount;
  private LocalDateTime createdAt;
  private String staffName;
  private String bankName;
  private String bankAccountLast4;
  private String phone;
}
