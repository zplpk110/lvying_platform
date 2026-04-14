package com.lvying.service;

import com.lvying.config.LvyingProperties;
import com.lvying.repo.ExpenseRepository;
import com.lvying.repo.IncomeRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundService {

  private final IncomeRepository incomeRepository;
  private final ExpenseRepository expenseRepository;
  private final LvyingProperties properties;

  @Transactional(readOnly = true)
  public BigDecimal totalActualIncome() {
    return nz(incomeRepository.sumAllAmount());
  }

  @Transactional(readOnly = true)
  public BigDecimal totalPaidCost() {
    return nz(expenseRepository.sumPaidCostGlobal());
  }

  @Transactional(readOnly = true)
  public BigDecimal totalPendingCost() {
    return nz(expenseRepository.sumPendingStaffApprovedGlobal());
  }

  @Transactional(readOnly = true)
  public BigDecimal availableBalance() {
    return totalActualIncome()
        .subtract(totalPaidCost())
        .subtract(totalPendingCost())
        .subtract(nz(properties.getReservedSafeCash()));
  }

  @Transactional(readOnly = true)
  public BigDecimal tourIncomeSum(UUID tourId) {
    return nz(incomeRepository.sumAmountByTourId(tourId));
  }

  @Transactional(readOnly = true)
  public BigDecimal tourPaidCost(UUID tourId) {
    return nz(expenseRepository.sumPaidCostByTour(tourId));
  }

  @Transactional(readOnly = true)
  public BigDecimal tourPendingStaffCost(UUID tourId) {
    return nz(expenseRepository.sumPendingStaffByTour(tourId));
  }

  @Transactional(readOnly = true)
  public BigDecimal tourEstimatedTotalCost(UUID tourId) {
    return tourPaidCost(tourId).add(tourPendingStaffCost(tourId));
  }

  @Transactional(readOnly = true)
  public BigDecimal tourExpectedRevenue(int guestCount, BigDecimal pricePerGuest) {
    return BigDecimal.valueOf(guestCount).multiply(nz(pricePerGuest));
  }

  @Transactional(readOnly = true)
  public boolean wouldExceedBudgetRedline(UUID tourId, BigDecimal additional, BigDecimal redline) {
    BigDecimal after =
        tourPaidCost(tourId).add(tourPendingStaffCost(tourId)).add(nz(additional));
    return after.compareTo(nz(redline)) > 0;
  }

  private static BigDecimal nz(BigDecimal v) {
    return v == null ? BigDecimal.ZERO : v;
  }
}
