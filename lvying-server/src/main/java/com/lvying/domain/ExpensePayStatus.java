package com.lvying.domain;

/** 垫付费用是否已对员工完成打款（公账支出固定视为已付，可不关注此字段语义）。 */
public enum ExpensePayStatus {
  /** 尚未对垫付人打款。 */
  UNPAID,
  /** 财务已批量/单笔完成打款。 */
  PAID
}
