package com.lvying.service;

import com.lvying.domain.*;
import com.lvying.repo.ExpenseRepository;
import com.lvying.repo.TourRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final TourRepository tourRepository;
  private final ExpenseRepository expenseRepository;
  private final FundService fundService;

  @Transactional(readOnly = true)
  public BossHomeResponse bossHome() {
    long pendingApproval =
        expenseRepository.countByPaymentMethodAndApprovalStatus(
            PaymentMethod.STAFF_ADVANCE, ExpenseApprovalStatus.PENDING);
    LocalDate now = LocalDate.now();
    LocalDate in3 = now.plusDays(3);
    List<Tour> tours =
        tourRepository.findByStatusOrderByDepartureDateAsc(TourStatus.IN_PROGRESS);
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

  @Transactional(readOnly = true)
  public StaffHomeResponse staffHome(UUID userId) {
    List<Expense> lines = expenseRepository.findByStaffUserIdOrderByCreatedAtDesc(userId);
    BigDecimal pending =
        lines.stream()
            .filter(
                e ->
                    e.getPaymentMethod() == PaymentMethod.STAFF_ADVANCE
                        && e.getApprovalStatus() == ExpenseApprovalStatus.APPROVED
                        && e.getPayStatus() == ExpensePayStatus.UNPAID)
            .map(Expense::getAmount)
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
                      e.getTour().getTourCode(),
                      e.getTour().getName(),
                      e.getCategory().name(),
                      e.getAmount().toPlainString(),
                      e.getApprovalStatus().name(),
                      e.getPayStatus().name(),
                      st);
                })
            .toList();
    return new StaffHomeResponse(pending.toPlainString(), rows);
  }

  public record BossHomeResponse(Urgent urgent, FundPool fundPool, List<BoardRow> inProgressBoard) {}

  public record Urgent(int pendingReimbursementApprovals, int tailDueBeforeDeparture, int marginAlertTours) {}

  public record FundPool(String availableBalance, String staffAdvanceOutstanding, String estimatedMonthNetProfit) {}

  public record BoardRow(
      UUID id, String tourCode, String name, String departureDate, String incomeExpenseRatio, String netProfitEstimate) {}

  public record StaffHomeResponse(String pendingRepaymentTotal, List<StaffLine> recentLines) {}

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
