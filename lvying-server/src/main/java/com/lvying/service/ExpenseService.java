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

/**
 * 记支出/报销：公账即时入账为已付；员工垫付走审批与打款状态机。
 *
 * <p>超支时抛出 {@link com.lvying.web.error.BusinessException}，{@code code=OVERSPEND_GUARD}，老板需在请求中带 {@code
 * bossConfirmed=true} 二次确认。
 */
@Service
@RequiredArgsConstructor
public class ExpenseService {

  private final ExpenseRepository expenseRepository;
  private final TourRepository tourRepository;
  private final UserRepository userRepository;
  private final FundService fundService;

  /**
   * 创建支出记录并落库。
   *
   * @param user 当前登录人；公账时作为审批人记一笔，员工垫付为待审。
   */
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
