package com.lvying.domain;

/** 支出类别，与 PRD 随手记类别一致（展示给非会计用户）。 */
public enum ExpenseCategory {
  /** 住宿。 */
  LODGING,
  /** 餐饮。 */
  DINING,
  /** 门票。 */
  TICKET,
  /** 交通。 */
  TRANSPORT,
  /** 司导费。 */
  DRIVER_GUIDE,
  /** 未归类或其他杂费。 */
  OTHER
}
