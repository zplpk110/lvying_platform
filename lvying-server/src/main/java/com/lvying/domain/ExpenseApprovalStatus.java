package com.lvying.domain;

/** 员工垫付类报销的审批状态。 */
public enum ExpenseApprovalStatus {
  /** 待老板审批。 */
  PENDING,
  /** 已通过，若未打款则计入 Pending_Cost。 */
  APPROVED,
  /** 部分驳回（保留记录，业务上可配合备注）。 */
  PARTIAL_REJECT,
  /** 已拒绝，不计入有效成本汇总。 */
  REJECTED
}
