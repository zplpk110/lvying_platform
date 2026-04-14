package com.lvying.domain;

/**
 * 登录用户角色，决定首页视图与接口权限（Spring Security：{@code ROLE_*}）。
 */
public enum UserRole {
  /** 老板 / 财务：经营仪表盘、审批、催收、封团等。 */
  BOSS_FINANCE,
  /** 业务员 / 导游：记支出、看垫款、团队协作。 */
  SALES_GUIDE
}
