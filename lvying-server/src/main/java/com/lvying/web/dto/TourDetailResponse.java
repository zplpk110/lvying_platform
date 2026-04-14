package com.lvying.web.dto;

import com.lvying.domain.TourStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

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
