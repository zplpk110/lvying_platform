package com.ruoyi.system.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.MvpFinanceRecord;
import com.ruoyi.system.domain.MvpGroup;

public interface LyFinanceMapper
{
    Map<String, Object> selectBossDashboard();

    List<Map<String, Object>> selectOngoingTourBoard();

    Map<String, Object> selectTourDetailById(@Param("groupId") Long groupId);

    BigDecimal selectGroupShouldReceivable(@Param("groupId") Long groupId);

    BigDecimal selectActualIncomeByGroupId(@Param("groupId") Long groupId);

    BigDecimal selectCostByGroupId(@Param("groupId") Long groupId);

    List<Map<String, Object>> selectGroupCustomers(@Param("groupId") Long groupId);

    List<Map<String, Object>> selectGroupFinanceRecords(@Param("groupId") Long groupId);

    int insertGroup(MvpGroup group);

    int insertFinanceRecord(MvpFinanceRecord record);

    int updateCustomerPaidAmount(@Param("customerId") Long customerId, @Param("amount") BigDecimal amount);

    List<Map<String, Object>> selectMyWallet(@Param("userId") Long userId, @Param("settleMonth") String settleMonth);

    BigDecimal selectMyWalletPendingAmount(@Param("userId") Long userId, @Param("settleMonth") String settleMonth);

    List<Map<String, Object>> selectAdvanceSummaryByMonth(@Param("settleMonth") String settleMonth);

    BigDecimal selectAdvanceTotalByUserMonth(@Param("userId") Long userId, @Param("settleMonth") String settleMonth);

    BigDecimal selectSettledAmountByUserMonth(@Param("userId") Long userId, @Param("settleMonth") String settleMonth);

    int insertAdvanceSettlement(Map<String, Object> params);

    List<Map<String, Object>> selectAdvanceSettlementList(@Param("settleMonth") String settleMonth);

    List<Map<String, Object>> selectOverdueReceivableTours();
}
