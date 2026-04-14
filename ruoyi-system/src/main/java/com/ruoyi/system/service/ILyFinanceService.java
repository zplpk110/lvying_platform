package com.ruoyi.system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.MvpGroup;

public interface ILyFinanceService
{
    Map<String, Object> getBossDashboard();

    Map<String, Object> getTourDetail(Long groupId);

    MvpGroup openGroup(MvpGroup group);

    Map<String, Object> addIncome(Long groupId, BigDecimal amount, String bizType, Long customerId, String payerName,
            String remark, String operator);

    Map<String, Object> addExpense(Long groupId, BigDecimal amount, String bizType, Long advanceUserId, String receiptUrl,
            String remark, String operator);

    Map<String, Object> getMyWallet(Long userId);

    List<Map<String, Object>> getAdvanceSummary(String settleMonth);

    Map<String, Object> settleAdvance(Long userId, String settleMonth, BigDecimal paidAmount, Long operatorId,
            String operatorName, String remark);

    List<Map<String, Object>> getAdvanceSettlementList(String settleMonth);

    List<Map<String, Object>> getOverdueReceivableTours();
}
