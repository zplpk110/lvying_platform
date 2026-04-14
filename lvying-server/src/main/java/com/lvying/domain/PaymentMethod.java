package com.lvying.domain;

/**
 * 支付方式，决定费用计入「已付」还是「待审/待付」链路。
 */
public enum PaymentMethod {
  /** 公账支付：创建即已审批且已付，全额计入 Paid_Cost。 */
  COMPANY_ACCOUNT,
  /** 员工垫付：需审批；通过后未打款前计入全库 Pending_Cost。 */
  STAFF_ADVANCE
}
