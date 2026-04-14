package com.lvying.service;

import com.lvying.config.LvyingProperties;
import com.lvying.repo.ExpenseRepository;
import com.lvying.repo.IncomeRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 全库与单团资金口径聚合服务，与 PRD「三态资金」一致。
 *
 * <ul>
 *   <li><b>Actual_Income（实收）</b>：{@link #totalActualIncome()} = 全表 {@link com.lvying.domain.Income} 金额之和。
 *   <li><b>Paid_Cost（已付成本）</b>：{@link #totalPaidCost()} = 公账支出 + 已打款的员工垫付（排除已驳回）。
 *   <li><b>Pending_Cost（待付垫付）</b>：{@link #totalPendingCost()} = 员工垫付且已审批通过、尚未打款。
 * </ul>
 *
 * <p><b>老板可用余额</b>：{@link #availableBalance()} = 实收 − 已付 − 待付垫付 −
 * {@link LvyingProperties#getReservedSafeCash() 安全预留金}（配置项 {@code lvying.reserved-safe-cash}）。
 *
 * <p>单团维度用于详情页「收银台 / 成本夹」与超支判断：已付 + 待垫付（含未审）与 {@link com.lvying.domain.Tour#getBudgetRedline()
 * 预算红线} 比较，见 {@link #wouldExceedBudgetRedline(UUID, BigDecimal, BigDecimal)}。
 */
@Service
@RequiredArgsConstructor
public class FundService {

  private final IncomeRepository incomeRepository;
  private final ExpenseRepository expenseRepository;
  private final LvyingProperties properties;

  /** 全库实收现金合计（PRD：Actual_Income）。 */
  @Transactional(readOnly = true)
  public BigDecimal totalActualIncome() {
    return nz(incomeRepository.sumAllAmount());
  }

  /** 全库已付固定成本合计（PRD：Paid_Cost，公账 + 已打款垫付）。 */
  @Transactional(readOnly = true)
  public BigDecimal totalPaidCost() {
    return nz(expenseRepository.sumPaidCostGlobal());
  }

  /**
   * 全库待付报销款（PRD：Pending_Cost）：员工垫付、已通过审批、财务尚未打款。
   *
   * <p>对应老板视角「欠员工垫款」；也参与可用余额扣减。
   */
  @Transactional(readOnly = true)
  public BigDecimal totalPendingCost() {
    return nz(expenseRepository.sumPendingStaffApprovedGlobal());
  }

  /**
   * 老板首页「可用余额」：口袋里能动用的钱（已扣预留金与待打款垫付）。
   *
   * @see LvyingProperties#getReservedSafeCash()
   */
  @Transactional(readOnly = true)
  public BigDecimal availableBalance() {
    return totalActualIncome()
        .subtract(totalPaidCost())
        .subtract(totalPendingCost())
        .subtract(nz(properties.getReservedSafeCash()));
  }

  /** 指定团已确认收款合计。 */
  @Transactional(readOnly = true)
  public BigDecimal tourIncomeSum(UUID tourId) {
    return nz(incomeRepository.sumAmountByTourId(tourId));
  }

  /** 指定团已付成本：公账 + 已对员工打款的垫付。 */
  @Transactional(readOnly = true)
  public BigDecimal tourPaidCost(UUID tourId) {
    return nz(expenseRepository.sumPaidCostByTour(tourId));
  }

  /**
   * 指定团「待支付/待报销」垫付：员工垫付且（待审或已通过）且未打款，用于成本预估与超支拦截。
   *
   * <p>注意：未审垫付不计入全库 {@link #totalPendingCost()}，但计入本团预估成本，避免边审边超支。
   */
  @Transactional(readOnly = true)
  public BigDecimal tourPendingStaffCost(UUID tourId) {
    return nz(expenseRepository.sumPendingStaffByTour(tourId));
  }

  /** 团维度总成本预估 = 已付 + 上项待垫付。 */
  @Transactional(readOnly = true)
  public BigDecimal tourEstimatedTotalCost(UUID tourId) {
    return tourPaidCost(tourId).add(tourPendingStaffCost(tourId));
  }

  /** 团应收上限 = 人数 × 单价（PRD 总应收）。 */
  @Transactional(readOnly = true)
  public BigDecimal tourExpectedRevenue(int guestCount, BigDecimal pricePerGuest) {
    return BigDecimal.valueOf(guestCount).multiply(nz(pricePerGuest));
  }

  /**
   * 若在本团已确认成本（已付 + 待垫付）基础上再增加一笔 {@code additional}，是否超过红线。
   *
   * @param redline 团的 {@link com.lvying.domain.Tour#getBudgetRedline()}
   * @return true 表示超支，业务层可要求老板 {@code bossConfirmed}
   */
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
