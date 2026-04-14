package com.lvying.mapper;

import com.lvying.domain.Expense;
import com.lvying.mapper.dto.ExpenseMonthExportRow;
import com.lvying.mapper.dto.ExpensePendingRow;
import com.lvying.mapper.dto.ExpenseStaffListRow;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExpenseMapper {

  Expense selectById(@Param("id") UUID id);

  BigDecimal sumPaidCostGlobal();

  BigDecimal sumPendingStaffApprovedGlobal();

  BigDecimal sumPaidCostByTour(@Param("tourId") UUID tourId);

  BigDecimal sumPendingStaffByTour(@Param("tourId") UUID tourId);

  long countByPaymentMethodAndApprovalStatus(
      @Param("paymentMethod") String paymentMethod,
      @Param("approvalStatus") String approvalStatus);

  List<ExpensePendingRow> selectPendingReimbursementForAggregation();

  List<ExpenseStaffListRow> selectByStaffUserIdOrderByCreatedAtDesc(
      @Param("staffUserId") UUID staffUserId);

  List<ExpenseMonthExportRow> selectApprovedUnpaidStaffInRange(
      @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  int insert(Expense expense);

  int update(Expense expense);
}
