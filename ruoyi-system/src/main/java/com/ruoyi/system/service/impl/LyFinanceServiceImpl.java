package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.mapper.LyFinanceMapper;
import com.ruoyi.system.service.ILyFinanceService;

@Service
public class LyFinanceServiceImpl implements ILyFinanceService
{
    private static final BigDecimal DEFAULT_RESERVE_FUND = new BigDecimal("10000");

    @Autowired
    private LyFinanceMapper lyFinanceMapper;

    @Override
    public Map<String, Object> getBossDashboard()
    {
        Map<String, Object> overview = lyFinanceMapper.selectBossDashboard(DEFAULT_RESERVE_FUND);
        overview.put("ongoingTours", lyFinanceMapper.selectOngoingTourBoard());
        return overview;
    }

    @Override
    public Map<String, Object> getTourDetail(Long tourId)
    {
        Map<String, Object> detail = lyFinanceMapper.selectTourDetailById(tourId);
        if (detail == null)
        {
            return null;
        }

        BigDecimal actualIncome = defaultZero(lyFinanceMapper.selectActualIncomeByTourId(tourId));
        BigDecimal paidCost = defaultZero(lyFinanceMapper.selectPaidCostByTourId(tourId));
        BigDecimal pendingCost = defaultZero(lyFinanceMapper.selectPendingCostByTourId(tourId));
        BigDecimal estimatedTotalCost = paidCost.add(pendingCost);
        BigDecimal estimatedProfit = actualIncome.subtract(estimatedTotalCost);
        BigDecimal commissionRate = new BigDecimal("0.15");

        detail.put("actualIncome", actualIncome);
        detail.put("paidCost", paidCost);
        detail.put("pendingCost", pendingCost);
        detail.put("estimatedTotalCost", estimatedTotalCost);
        detail.put("estimatedProfit", estimatedProfit);
        detail.put("commissionRate", commissionRate);
        detail.put("estimatedCommission", estimatedProfit.multiply(commissionRate).max(BigDecimal.ZERO));
        return detail;
    }

    @Override
    public Map<String, Object> addIncome(Long tourId, BigDecimal amount, String incomeType, String remark)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("tourId", tourId);
        params.put("amount", amount);
        params.put("incomeType", StringUtils.isBlank(incomeType) ? "other" : incomeType);
        params.put("remark", remark);
        params.put("receivedTime", DateUtils.getNowDate());
        lyFinanceMapper.insertIncome(params);
        return getTourDetail(tourId);
    }

    @Override
    public Map<String, Object> addExpense(Long tourId, BigDecimal amount, String category, String paymentMethod,
            String advanceUserName, String receiptUrl, String remark, boolean forceConfirm)
    {
        Map<String, Object> current = getTourDetail(tourId);
        if (current == null)
        {
            return null;
        }

        BigDecimal budgetLimit = defaultZero((BigDecimal) current.get("budgetLimit"));
        BigDecimal estimatedTotalCost = defaultZero((BigDecimal) current.get("estimatedTotalCost"));
        BigDecimal afterSubmit = estimatedTotalCost.add(defaultZero(amount));
        boolean overBudget = afterSubmit.compareTo(budgetLimit) > 0;
        if (overBudget && !forceConfirm)
        {
            Map<String, Object> warning = new HashMap<>();
            warning.put("needOwnerConfirm", true);
            warning.put("message", "操作将使本团毛利低于预期，需老板手机确认通过后方可记录。");
            warning.put("currentEstimatedTotalCost", estimatedTotalCost);
            warning.put("budgetLimit", budgetLimit);
            warning.put("afterSubmitCost", afterSubmit);
            return warning;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("tourId", tourId);
        params.put("amount", amount);
        params.put("category", category);
        params.put("paymentMethod", paymentMethod);
        params.put("advanceUserName", advanceUserName);
        params.put("receiptUrl", receiptUrl);
        params.put("remark", remark);
        params.put("status", "PUBLIC".equalsIgnoreCase(paymentMethod) ? "PAID" : "PENDING");
        params.put("occurDate", DateUtils.getNowDate());
        lyFinanceMapper.insertCost(params);

        Map<String, Object> result = getTourDetail(tourId);
        result.put("needOwnerConfirm", false);
        result.put("overBudget", overBudget);
        return result;
    }

    @Override
    public Map<String, Object> getMyWallet(String userName)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("pendingAmount", defaultZero(lyFinanceMapper.selectMyWalletPendingAmount(userName)));
        result.put("items", lyFinanceMapper.selectMyWallet(userName));
        return result;
    }

    @Override
    public List<Map<String, Object>> getReimbursementApprovals()
    {
        return lyFinanceMapper.selectReimbursementApprovals();
    }

    @Override
    public int approveReimbursement(Long costId, String remark, String operator)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("costId", costId);
        params.put("status", "PAID");
        params.put("approvalRemark", remark);
        params.put("updateBy", operator);
        return lyFinanceMapper.updateReimbursementStatus(params);
    }

    @Override
    public int rejectReimbursement(Long costId, String remark, String operator)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("costId", costId);
        params.put("status", "REJECTED");
        params.put("approvalRemark", remark);
        params.put("updateBy", operator);
        return lyFinanceMapper.updateReimbursementStatus(params);
    }

    @Override
    public List<Map<String, Object>> getOverdueReceivableTours()
    {
        return lyFinanceMapper.selectOverdueReceivableTours();
    }

    private BigDecimal defaultZero(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }
}
