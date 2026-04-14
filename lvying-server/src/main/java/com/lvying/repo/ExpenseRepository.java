package com.lvying.repo;

import com.lvying.domain.Expense;
import com.lvying.domain.ExpenseApprovalStatus;
import com.lvying.domain.ExpensePayStatus;
import com.lvying.domain.PaymentMethod;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 支出聚合查询：全库/按团的「已付成本」「待垫付」「待审批聚合列表」「月结已审未付」等，供 {@link com.lvying.service.FundService}与报销模块使用。
 *
 * <p>JPQL 中枚举以字符串字面量存储形态编写（与 {@link jakarta.persistence.EnumType#STRING} 一致）。
 */
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

  @Query(
      "SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.approvalStatus <> 'REJECTED' "
          + "AND (e.paymentMethod = 'COMPANY_ACCOUNT' "
          + "OR (e.paymentMethod = 'STAFF_ADVANCE' AND e.payStatus = 'PAID'))")
  BigDecimal sumPaidCostGlobal();

  @Query(
      "SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.paymentMethod = 'STAFF_ADVANCE' "
          + "AND e.approvalStatus = 'APPROVED' AND e.payStatus = 'UNPAID'")
  BigDecimal sumPendingStaffApprovedGlobal();

  @Query(
      "SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.tour.id = :tourId "
          + "AND e.approvalStatus <> 'REJECTED' "
          + "AND (e.paymentMethod = 'COMPANY_ACCOUNT' "
          + "OR (e.paymentMethod = 'STAFF_ADVANCE' AND e.payStatus = 'PAID'))")
  BigDecimal sumPaidCostByTour(@Param("tourId") UUID tourId);

  @Query(
      "SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.tour.id = :tourId "
          + "AND e.paymentMethod = 'STAFF_ADVANCE' "
          + "AND e.approvalStatus IN ('PENDING', 'APPROVED') AND e.payStatus = 'UNPAID'")
  BigDecimal sumPendingStaffByTour(@Param("tourId") UUID tourId);

  long countByPaymentMethodAndApprovalStatus(
      PaymentMethod paymentMethod, ExpenseApprovalStatus approvalStatus);

  @Query(
      "SELECT e FROM Expense e JOIN FETCH e.tour JOIN FETCH e.staffUser WHERE e.paymentMethod = 'STAFF_ADVANCE' "
          + "AND e.approvalStatus IN ('PENDING', 'APPROVED') AND e.payStatus = 'UNPAID'")
  List<Expense> findPendingReimbursementForAggregation();

  @EntityGraph(attributePaths = "tour")
  List<Expense> findByStaffUserIdOrderByCreatedAtDesc(UUID staffUserId);

  @Query(
      "SELECT e FROM Expense e JOIN FETCH e.staffUser s WHERE e.paymentMethod = 'STAFF_ADVANCE' "
          + "AND e.approvalStatus = 'APPROVED' AND e.payStatus = 'UNPAID' "
          + "AND e.createdAt >= :start AND e.createdAt <= :end")
  List<Expense> findApprovedUnpaidStaffInRange(
      @Param("start") Instant start, @Param("end") Instant end);
}
