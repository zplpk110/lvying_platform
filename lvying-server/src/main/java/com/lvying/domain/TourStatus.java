package com.lvying.domain;

/** 团期生命周期状态。 */
public enum TourStatus {
  /** 草稿（MVP 较少使用，可直接进行中）。 */
  DRAFT,
  /** 进行中：收款、成本、报销持续入账。 */
  IN_PROGRESS,
  /** 已封团结算，不再参与日常流水。 */
  SETTLED,
  /** 已取消。 */
  CANCELLED
}
