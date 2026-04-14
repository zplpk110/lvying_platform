package com.lvying.service;

import com.lvying.domain.*;
import com.lvying.mapper.ExpenseMapper;
import com.lvying.mapper.dto.ExpenseMonthExportRow;
import com.lvying.mapper.dto.ExpensePendingRow;
import com.lvying.web.error.BusinessException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 极速报销：按「团 + 垫付人」聚合待办；审批、部分驳回；按月筛选已审未付生成代发 CSV；批量标记已打款。
 */
@Service
@RequiredArgsConstructor
public class ReimbursementService {

  private final ExpenseMapper expenseMapper;

  /** 老板审批页：待付/待审的员工垫付，按团号与业务员折叠。 */
  @Transactional(readOnly = true)
  public List<PendingGroup> pendingByTour() {
    List<ExpensePendingRow> list = expenseMapper.selectPendingReimbursementForAggregation();
    record Key(UUID tourId, UUID staffId) {}
    Map<Key, List<ExpensePendingRow>> map = new LinkedHashMap<>();
    for (ExpensePendingRow e : list) {
      Key k = new Key(e.getTourId(), e.getStaffUserId());
      map.computeIfAbsent(k, x -> new ArrayList<>()).add(e);
    }
    return map.entrySet().stream()
        .map(
            en -> {
              List<ExpensePendingRow> lines = en.getValue();
              ExpensePendingRow first = lines.get(0);
              BigDecimal total =
                  lines.stream()
                      .map(ExpensePendingRow::getAmount)
                      .reduce(BigDecimal.ZERO, BigDecimal::add);
              return new PendingGroup(
                  first.getTourId(),
                  first.getTourCode(),
                  first.getTourName(),
                  first.getStaffUserId(),
                  first.getStaffName(),
                  total.toPlainString(),
                  lines.stream()
                      .map(
                          l ->
                              new Line(
                                  l.getId(),
                                  l.getAmount().toPlainString(),
                                  l.getCategory().name(),
                                  l.getReceiptImageUrl(),
                                  l.getApprovalStatus().name(),
                                  l.getCreatedAt().toString()))
                      .toList());
            })
        .toList();
  }

  /** 单条通过：进入「已审未付」，计入全库 Pending_Cost直至财务打款。 */
  @Transactional
  public void approve(UUID expenseId, UUID approverId) {
    Expense e = expenseMapper.selectById(expenseId);
    if (e == null) {
      throw new BusinessException("NOT_FOUND", "记录不存在");
    }
    if (e.getPaymentMethod() != PaymentMethod.STAFF_ADVANCE) {
      throw new IllegalArgumentException("非员工垫付");
    }
    LocalDateTime now = LocalDateTime.now();
    e.setApprovalStatus(ExpenseApprovalStatus.APPROVED);
    e.setApprovedById(approverId);
    e.setApprovedAt(now);
    e.setUpdatedAt(now);
    expenseMapper.update(e);
  }

  /** 部分驳回：状态 {@link ExpenseApprovalStatus#PARTIAL_REJECT}，并写入备注。 */
  @Transactional
  public void partialReject(UUID expenseId, UUID approverId, String note) {
    Expense e = expenseMapper.selectById(expenseId);
    if (e == null) {
      throw new BusinessException("NOT_FOUND", "记录不存在");
    }
    LocalDateTime now = LocalDateTime.now();
    e.setApprovalStatus(ExpenseApprovalStatus.PARTIAL_REJECT);
    e.setApprovedById(approverId);
    e.setApprovedAt(now);
    e.setNote(note);
    e.setUpdatedAt(now);
    expenseMapper.update(e);
  }

  /**
   * 月结代发：筛选当月创建、已审未付的员工垫付，按员工汇总金额与银行卡信息，返回行数据 + CSV 文本（上传网银用）。
   */
  @Transactional(readOnly = true)
  public BatchExportResponse batchExport(int year, int month) {
    LocalDate start = LocalDate.of(year, month, 1);
    LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
    LocalDateTime from = start.atStartOfDay();
    LocalDateTime to = LocalDateTime.of(end, LocalTime.MAX);
    List<ExpenseMonthExportRow> expenses =
        expenseMapper.selectApprovedUnpaidStaffInRange(from, to);
    Map<UUID, Agg> byStaff = new LinkedHashMap<>();
    for (ExpenseMonthExportRow row : expenses) {
      UUID sid = row.getStaffUserId();
      byStaff
          .computeIfAbsent(
              sid,
              id ->
                  new Agg(
                      row.getStaffName(),
                      row.getBankName() != null ? row.getBankName() : "",
                      row.getBankAccountLast4() != null ? row.getBankAccountLast4() : "",
                      row.getPhone(),
                      BigDecimal.ZERO))
          .add(row.getAmount());
    }
    List<BatchExportResponse.Row> rows =
        byStaff.values().stream()
            .map(
                a ->
                    new BatchExportResponse.Row(
                        a.name,
                        a.sum.toPlainString(),
                        a.bankName,
                        a.accountLast4,
                        a.phone))
            .toList();
    String header = "姓名,金额,开户行,账号尾号,手机";
    String csv =
        header
            + "\n"
            + rows.stream()
                .map(
                    r ->
                        String.join(
                            ",",
                            r.staffName(),
                            r.totalAmount(),
                            r.bankName(),
                            r.accountLast4(),
                            r.phone()))
                .collect(Collectors.joining("\n"));
    return new BatchExportResponse(
        year + "-" + String.format("%02d", month), rows, csv);
  }

  /** 财务完成批量转账后，将对应报销单置为已付并记录批次号。 */
  @Transactional
  public void markBatchPaid(List<UUID> expenseIds, String batchRef) {
    LocalDateTime now = LocalDateTime.now();
    for (UUID id : expenseIds) {
      Expense e = expenseMapper.selectById(id);
      if (e != null
          && e.getApprovalStatus() == ExpenseApprovalStatus.APPROVED
          && e.getPayStatus() == ExpensePayStatus.UNPAID) {
        e.setPayStatus(ExpensePayStatus.PAID);
        e.setBatchPayRef(batchRef);
        e.setUpdatedAt(now);
        expenseMapper.update(e);
      }
    }
  }

  /** 按团 + 垫付人聚合的审批视图。 */
  public record PendingGroup(
      UUID tourId,
      String tourCode,
      String tourName,
      UUID staffUserId,
      String staffName,
      String totalAdvance,
      List<Line> lines) {}

  /** 聚合内单笔垫付明细。 */
  public record Line(
      UUID id,
      String amount,
      String category,
      String receiptImageUrl,
      String approvalStatus,
      String createdAt) {}

  /** 月结导出：期间标识、行列表、可直接下载的 CSV 全文。 */
  public record BatchExportResponse(String period, List<Row> rows, String csv) {
    /** 每位员工一行汇总（网银代发）。 */
    public record Row(
        String staffName, String totalAmount, String bankName, String accountLast4, String phone) {}
  }

  /** 月结按员工累加金额的临时结构。 */
  private static class Agg {
    final String name;
    final String bankName;
    final String accountLast4;
    final String phone;
    BigDecimal sum;

    Agg(String name, String bankName, String accountLast4, String phone, BigDecimal sum) {
      this.name = name;
      this.bankName = bankName;
      this.accountLast4 = accountLast4;
      this.phone = phone;
      this.sum = sum;
    }

    void add(BigDecimal a) {
      sum = sum.add(a);
    }
  }
}
