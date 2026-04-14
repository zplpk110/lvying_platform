package com.lvying.service;

import com.lvying.domain.*;
import com.lvying.mapper.ExpenseMapper;
import com.lvying.mapper.TourMapper;
import com.lvying.mapper.dto.ExpenseStaffListRow;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 首页聚合：老板「经营仪表盘」与业务员「我的任务与垫资」。
 *
 * <p>紧急待办、资金池大数字均依赖 {@link FundService} 与团/报销笔数统计。
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

  private final TourMapper tourMapper;
  private final ExpenseMapper expenseMapper;
  private final FundService fundService;

  /**
   * 老板首页：待审批笔数、出团前 3 天内仍有尾欠的团数、毛利/成本预警团数、可用余额、在途垫款、本月预估净利、进行中看板行。
   */
  @Transactional(readOnly = true)
  public BossHomeResponse bossHome() {
    long pendingApproval =
        expenseMapper.countByPaymentMethodAndApprovalStatus(
            PaymentMethod.STAFF_ADVANCE.name(), ExpenseApprovalStatus.PENDING.name());
    LocalDate now = LocalDate.now();
    LocalDate in3 = now.plusDays(3);
    List<Tour> tours =
        tourMapper.selectByStatusOrderByDepartureDate(TourStatus.IN_PROGRESS.name());
    int tailDue = 0;
    int marginAlert = 0;
    List<BoardRow> board = new ArrayList<>();
    LocalDate monthStart = now.withDayOfMonth(1);
    LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
    BigDecimal monthNet = BigDecimal.ZERO;

    for (Tour t : tours) {
      UUID id = t.getId();
      BigDecimal incomeSum = fundService.tourIncomeSum(id);
      BigDecimal receivable =
          BigDecimal.valueOf(t.getGuestCount()).multiply(t.getPricePerGuest());
      BigDecimal tail = receivable.subtract(incomeSum);
      BigDecimal paid = fundService.tourPaidCost(id);
      BigDecimal pend = fundService.tourPendingStaffCost(id);
      BigDecimal est = paid.add(pend);
      BigDecimal redline = t.getBudgetRedline();
      BigDecimal net = incomeSum.subtract(est);

      if (tail.compareTo(BigDecimal.ZERO) > 0
          && !t.getDepartureDate().isBefore(now)
          && !t.getDepartureDate().isAfter(in3)) {
        tailDue++;
      }
      if (est.compareTo(redline) > 0 || net.compareTo(BigDecimal.ZERO) < 0) {
        marginAlert++;
      }
      String ratio =
          est.compareTo(BigDecimal.ZERO) == 0
              ? "—"
              : incomeSum.divide(est, 2, RoundingMode.HALF_UP).toPlainString() + ":1";
      board.add(
          new BoardRow(
              id,
              t.getTourCode(),
              t.getName(),
              t.getDepartureDate().toString(),
              ratio,
              net.toPlainString()));

      if (!t.getDepartureDate().isBefore(monthStart) && !t.getDepartureDate().isAfter(monthEnd)) {
        monthNet = monthNet.add(net);
      }
    }

    return new BossHomeResponse(
        new Urgent((int) pendingApproval, tailDue, marginAlert),
        new FundPool(
            fundService.availableBalance().toPlainString(),
            fundService.totalPendingCost().toPlainString(),
            monthNet.toPlainString()),
        board);
  }

  /**
   * 业务员首页：待公司还款总额（已审未打款垫付之和）+ 近期垫付流水（含展示状态文案）。
   */
  @Transactional(readOnly = true)
  public StaffHomeResponse staffHome(UUID userId) {
    List<ExpenseStaffListRow> lines =
        expenseMapper.selectByStaffUserIdOrderByCreatedAtDesc(userId);
    BigDecimal pending =
        lines.stream()
            .filter(
                e ->
                    e.getPaymentMethod() == PaymentMethod.STAFF_ADVANCE
                        && e.getApprovalStatus() == ExpenseApprovalStatus.APPROVED
                        && e.getPayStatus() == ExpensePayStatus.UNPAID)
            .map(ExpenseStaffListRow::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    List<StaffLine> rows =
        lines.stream()
            .filter(e -> e.getPaymentMethod() == PaymentMethod.STAFF_ADVANCE)
            .limit(50)
            .map(
                e -> {
                  String st;
                  if (e.getApprovalStatus() == ExpenseApprovalStatus.APPROVED
                      && e.getPayStatus() == ExpensePayStatus.UNPAID) {
                    st = "等待打款";
                  } else if (e.getPayStatus() == ExpensePayStatus.PAID) {
                    st = "已到账";
                  } else {
                    st = "待审批";
                  }
                  return new StaffLine(
                      e.getId(),
                      e.getCreatedAt().toString().substring(0, 10),
                      e.getTourCode(),
                      e.getTourName(),
                      e.getCategory().name(),
                      e.getAmount().toPlainString(),
                      e.getApprovalStatus().name(),
                      e.getPayStatus().name(),
                      st);
                })
            .toList();
    return new StaffHomeResponse(pending.toPlainString(), rows);
  }

  /** 老板首页完整 JSON 模型。 */
  public record BossHomeResponse(Urgent urgent, FundPool fundPool, List<BoardRow> inProgressBoard) {}

  /** 今日紧急待办三类计数（报销待审、尾款临期、毛利预警）。 */
  public record Urgent(int pendingReimbursementApprovals, int tailDueBeforeDeparture, int marginAlertTours) {}

  /** 资金池：可用余额、在途垫款、本月预估净利（字符串便于前端直接展示）。 */
  public record FundPool(String availableBalance, String staffAdvanceOutstanding, String estimatedMonthNetProfit) {}

  /** 进行中团看板一行。 */
  public record BoardRow(
      UUID id, String tourCode, String name, String departureDate, String incomeExpenseRatio, String netProfitEstimate) {}

  /** 业务员首页：待还款总额 + 近期垫付。 */
  public record StaffHomeResponse(String pendingRepaymentTotal, List<StaffLine> recentLines) {}

  /** 业务员垫付流水一行（含展示用状态文案）。 */
  public record StaffLine(
      UUID id,
      String date,
      String tourCode,
      String tourName,
      String category,
      String amount,
      String approvalStatus,
      String payStatus,
      String displayStatus) {}
}
