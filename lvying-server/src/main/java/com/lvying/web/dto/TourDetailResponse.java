package com.lvying.web.dto;

import com.lvying.domain.TourStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 团详情 API模型：基本信息 + {@link Finance}（收银台/成本夹/结算卡所需金额均为字符串，避免 JSON 丢精度）。
 */
public record TourDetailResponse(
    UUID id,
    String tourCode,
    String name,
    TourStatus status,
    LocalDate departureDate,
    int guestCount,
    BigDecimal pricePerGuest,
    BigDecimal budgetRedline,
    BigDecimal grossMarginPct,
    LoginResponse.UserInfo salesUser,
    BigDecimal commissionRate,
    Finance finance) {

  /** 与 PRD 三栏对应的聚合金额（单位：元，十进制字符串）。 */
  public record Finance(
      String totalReceivable,
      String incomeReceived,
      String depositReceived,
      String balanceReceived,
      String tailOwed,
      String budgetRedline,
      String paidCost,
      String pendingStaffCost,
      String estimatedTotalCost,
      String netProfitEstimate,
      String budgetSurplus,
      String commissionRate,
      String commissionEstimate) {}
}
