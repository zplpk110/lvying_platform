package com.lvying.service;

import com.lvying.domain.*;
import com.lvying.repo.ExpenseRepository;
import com.lvying.repo.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReimbursementService {

  private final ExpenseRepository expenseRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public List<PendingGroup> pendingByTour() {
    List<Expense> list = expenseRepository.findPendingReimbursementForAggregation();
    record Key(UUID tourId, UUID staffId) {}
    Map<Key, List<Expense>> map = new LinkedHashMap<>();
    for (Expense e : list) {
      Key k = new Key(e.getTour().getId(), e.getStaffUser().getId());
      map.computeIfAbsent(k, x -> new ArrayList<>()).add(e);
    }
    return map.entrySet().stream()
        .map(
            en -> {
              List<Expense> lines = en.getValue();
              Expense first = lines.get(0);
              BigDecimal total =
                  lines.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
              return new PendingGroup(
                  first.getTour().getId(),
                  first.getTour().getTourCode(),
                  first.getTour().getName(),
                  first.getStaffUser().getId(),
                  first.getStaffUser().getName(),
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

  @Transactional
  public void approve(UUID expenseId, UUID approverId) {
    Expense e =
        expenseRepository
            .findById(expenseId)
            .orElseThrow(() -> new EntityNotFoundException("记录不存在"));
    if (e.getPaymentMethod() != PaymentMethod.STAFF_ADVANCE) {
      throw new IllegalArgumentException("非员工垫付");
    }
    e.setApprovalStatus(ExpenseApprovalStatus.APPROVED);
    e.setApprovedBy(userRepository.getReferenceById(approverId));
    e.setApprovedAt(Instant.now());
    expenseRepository.save(e);
  }

  @Transactional
  public void partialReject(UUID expenseId, UUID approverId, String note) {
    Expense e =
        expenseRepository
            .findById(expenseId)
            .orElseThrow(() -> new EntityNotFoundException("记录不存在"));
    e.setApprovalStatus(ExpenseApprovalStatus.PARTIAL_REJECT);
    e.setApprovedBy(userRepository.getReferenceById(approverId));
    e.setApprovedAt(Instant.now());
    e.setNote(note);
    expenseRepository.save(e);
  }

  @Transactional(readOnly = true)
  public BatchExportResponse batchExport(int year, int month) {
    LocalDate start = LocalDate.of(year, month, 1);
    LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
    ZoneId z = ZoneId.systemDefault();
    Instant from = start.atStartOfDay(z).toInstant();
    Instant to = end.plusDays(1).atStartOfDay(z).minusNanos(1).toInstant();
    List<Expense> expenses = expenseRepository.findApprovedUnpaidStaffInRange(from, to);
    Map<UUID, Agg> byStaff = new LinkedHashMap<>();
    for (Expense e : expenses) {
      User u = e.getStaffUser();
      byStaff
          .computeIfAbsent(
              u.getId(),
              id ->
                  new Agg(
                      u.getName(),
                      u.getBankName() != null ? u.getBankName() : "",
                      u.getBankAccountLast4() != null ? u.getBankAccountLast4() : "",
                      u.getPhone(),
                      BigDecimal.ZERO))
          .add(e.getAmount());
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
        header            + "\n"
            + rows.stream()
                .map(r -> String.join(",", r.staffName(), r.totalAmount(), r.bankName(), r.accountLast4(), r.phone()))
                .collect(Collectors.joining("\n"));
    return new BatchExportResponse(
        year + "-" + String.format("%02d", month), rows, csv);
  }

  @Transactional
  public void markBatchPaid(List<UUID> expenseIds, String batchRef) {
    List<Expense> list = expenseRepository.findAllById(expenseIds);
    for (Expense e : list) {
      if (e.getApprovalStatus() == ExpenseApprovalStatus.APPROVED
          && e.getPayStatus() == ExpensePayStatus.UNPAID) {
        e.setPayStatus(ExpensePayStatus.PAID);
        e.setBatchPayRef(batchRef);
      }
    }
    expenseRepository.saveAll(list);
  }

  public record PendingGroup(
      UUID tourId,
      String tourCode,
      String tourName,
      UUID staffUserId,
      String staffName,
      String totalAdvance,
      List<Line> lines) {}

  public record Line(
      UUID id,
      String amount,
      String category,
      String receiptImageUrl,
      String approvalStatus,
      String createdAt) {}

  public record BatchExportResponse(String period, List<Row> rows, String csv) {
    public record Row(
        String staffName, String totalAmount, String bankName, String accountLast4, String phone) {}
  }

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
