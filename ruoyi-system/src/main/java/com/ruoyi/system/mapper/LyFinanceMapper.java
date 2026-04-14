package com.ruoyi.system.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface LyFinanceMapper
{
    Map<String, Object> selectBossDashboard(@Param("reserveFund") BigDecimal reserveFund);

    List<Map<String, Object>> selectOngoingTourBoard();

    Map<String, Object> selectTourDetailById(@Param("tourId") Long tourId);

    int insertIncome(Map<String, Object> params);

    int insertCost(Map<String, Object> params);

    BigDecimal selectPaidCostByTourId(@Param("tourId") Long tourId);

    BigDecimal selectPendingCostByTourId(@Param("tourId") Long tourId);

    BigDecimal selectActualIncomeByTourId(@Param("tourId") Long tourId);

    List<Map<String, Object>> selectMyWallet(@Param("advanceUserName") String advanceUserName);

    BigDecimal selectMyWalletPendingAmount(@Param("advanceUserName") String advanceUserName);

    List<Map<String, Object>> selectReimbursementApprovals();

    int updateReimbursementStatus(Map<String, Object> params);

    List<Map<String, Object>> selectOverdueReceivableTours();
}
