package com.lvying.service;

import com.lvying.domain.*;
import com.lvying.repo.ExpenseRepository;
import com.lvying.repo.TourRepository;
import com.lvying.repo.UserRepository;
import com.lvying.security.AppUserDetails;
import com.lvying.web.dto.CreateExpenseRequest;
import com.lvying.web.error.BusinessException;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

  private final ExpenseRepository expenseRepository;
  private final TourRepository tourRepository;
  private final UserRepository userRepository;
  private final FundService fundService;

  @Transactional
  public void create(UUID tourId, CreateExpenseRequest req, AppUserDetails user) {
    Tour tour =
        tourRepository.findById(tourId).orElseThrow(() -> new BusinessException("NOT_FOUND", "团不存在"));
    if (fundService.wouldExceedBudgetRedline(tourId, req.amount(), tour.getBudgetRedline())) {
      boolean bossOk =
          user.getRole() == UserRole.BOSS_FINANCE && Boolean.TRUE.equals(req.bossConfirmed());
      if (!bossOk) {
        throw new BusinessException(
            "OVERSPEND_GUARD", "操作将使本团毛利低于预期，需老板手机确认通过后方可记录。");
      }
    }
    if (req.paymentMethod() == PaymentMethod.STAFF_ADVANCE && req.staffUserId() == null) {
      throw new BusinessException("VALIDATION", "员工垫付需选择垫付人");
    }
    boolean company = req.paymentMethod() == PaymentMethod.COMPANY_ACCOUNT;
    User staff =
        req.paymentMethod() == PaymentMethod.STAFF_ADVANCE
            ? userRepository.getReferenceById(req.staffUserId())
            : null;
    User approver = company ? userRepository.getReferenceById(user.getId()) : null;
    Expense e =
        Expense.builder()
            .tour(tour)
            .amount(req.amount())
            .category(req.category())
            .paymentMethod(req.paymentMethod())
            .staffUser(staff)
            .receiptImageUrl(req.receiptImageUrl())
            .note(req.note())
            .approvalStatus(
                company ? ExpenseApprovalStatus.APPROVED : ExpenseApprovalStatus.PENDING)
            .payStatus(company ? ExpensePayStatus.PAID : ExpensePayStatus.UNPAID)
            .approvedBy(approver)
            .approvedAt(company ? Instant.now() : null)
            .build();
    expenseRepository.save(e);
  }
}
