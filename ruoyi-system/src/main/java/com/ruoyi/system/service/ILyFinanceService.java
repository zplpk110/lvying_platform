package com.ruoyi.system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ILyFinanceService
{
    Map<String, Object> getBossDashboard();

    Map<String, Object> getTourDetail(Long tourId);

    Map<String, Object> addIncome(Long tourId, BigDecimal amount, String incomeType, String remark);

    Map<String, Object> addExpense(Long tourId, BigDecimal amount, String category, String paymentMethod,
            String advanceUserName, String receiptUrl, String remark, boolean forceConfirm);

    Map<String, Object> getMyWallet(String userName);

    List<Map<String, Object>> getReimbursementApprovals();

    int approveReimbursement(Long costId, String remark, String operator);

    int rejectReimbursement(Long costId, String remark, String operator);

    List<Map<String, Object>> getOverdueReceivableTours();
}
