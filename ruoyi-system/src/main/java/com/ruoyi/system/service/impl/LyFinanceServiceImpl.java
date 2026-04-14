package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.MvpFinanceRecord;
import com.ruoyi.system.domain.MvpGroup;
import com.ruoyi.system.mapper.LyFinanceMapper;
import com.ruoyi.system.service.ILyFinanceService;

@Service
public class LyFinanceServiceImpl implements ILyFinanceService
{
    @Autowired
    private LyFinanceMapper lyFinanceMapper;

    @Override
    public Map<String, Object> getBossDashboard()
    {
        Map<String, Object> overview = lyFinanceMapper.selectBossDashboard();
        overview.put("ongoingTours", lyFinanceMapper.selectOngoingTourBoard());
        overview.put("overdueTours", lyFinanceMapper.selectOverdueReceivableTours());
        return overview;
    }

    @Override
    public Map<String, Object> getTourDetail(Long groupId)
    {
        Map<String, Object> detail = lyFinanceMapper.selectTourDetailById(groupId);
        if (detail == null)
        {
            return null;
        }

        BigDecimal shouldReceivable = defaultZero(lyFinanceMapper.selectGroupShouldReceivable(groupId));
        BigDecimal actualIncome = defaultZero(lyFinanceMapper.selectActualIncomeByGroupId(groupId));
        BigDecimal totalCost = defaultZero(lyFinanceMapper.selectCostByGroupId(groupId));
        BigDecimal estimatedProfit = shouldReceivable.subtract(totalCost);
        BigDecimal cashProfit = actualIncome.subtract(totalCost);
        BigDecimal budgetCost = defaultZero((BigDecimal) detail.get("budgetCost"));

        detail.put("shouldReceivable", shouldReceivable);
        detail.put("actualIncome", actualIncome);
        detail.put("totalCost", totalCost);
        detail.put("estimatedProfit", estimatedProfit);
        detail.put("cashProfit", cashProfit);
        detail.put("budgetWarnLevel", calcBudgetWarnLevel(totalCost, budgetCost));
        detail.put("customers", lyFinanceMapper.selectGroupCustomers(groupId));
        detail.put("records", lyFinanceMapper.selectGroupFinanceRecords(groupId));
        return detail;
    }

    @Override
    public MvpGroup openGroup(MvpGroup group)
    {
        if (group.getStatus() == null)
        {
            group.setStatus(1);
        }
        lyFinanceMapper.insertGroup(group);
        return group;
    }

    @Override
    public Map<String, Object> addIncome(Long groupId, BigDecimal amount, String bizType, Long customerId, String payerName,
            String remark, String operator)
    {
        MvpFinanceRecord record = new MvpFinanceRecord();
        record.setGroupId(groupId);
        record.setCustomerId(customerId);
        record.setRecordType(1);
        record.setBizType(StringUtils.isBlank(bizType) ? "OTHER_INCOME" : bizType);
        record.setAmount(amount);
        record.setOccurDate(DateUtils.getNowDate());
        record.setPayerName(payerName);
        record.setStatus(1);
        record.setRemark(remark);
        record.setCreateBy(operator);
        lyFinanceMapper.insertFinanceRecord(record);

        if (customerId != null)
        {
            lyFinanceMapper.updateCustomerPaidAmount(customerId, amount);
        }
        return getTourDetail(groupId);
    }

    @Override
    public Map<String, Object> addExpense(Long groupId, BigDecimal amount, String bizType, Long advanceUserId, String receiptUrl,
            String remark, String operator)
    {
        MvpFinanceRecord record = new MvpFinanceRecord();
        record.setGroupId(groupId);
        record.setRecordType(2);
        record.setBizType(StringUtils.isBlank(bizType) ? "OTHER_COST" : bizType);
        record.setAmount(amount);
        record.setOccurDate(DateUtils.getNowDate());
        record.setAdvanceUserId(advanceUserId);
        record.setVoucherUrl(receiptUrl);
        record.setStatus(1);
        record.setRemark(remark);
        record.setCreateBy(operator);
        lyFinanceMapper.insertFinanceRecord(record);
        return getTourDetail(groupId);
    }

    @Override
    public Map<String, Object> getMyWallet(Long userId)
    {
        String settleMonth = DateUtils.dateTimeNow(DateUtils.YYYY_MM);
        Map<String, Object> result = new HashMap<>();
        result.put("settleMonth", settleMonth);
        result.put("pendingAmount", defaultZero(lyFinanceMapper.selectMyWalletPendingAmount(userId, settleMonth)));
        result.put("items", lyFinanceMapper.selectMyWallet(userId, settleMonth));
        return result;
    }

    @Override
    public List<Map<String, Object>> getAdvanceSummary(String settleMonth)
    {
        return lyFinanceMapper.selectAdvanceSummaryByMonth(normalizeSettleMonth(settleMonth));
    }

    @Override
    public Map<String, Object> settleAdvance(Long userId, String settleMonth, BigDecimal paidAmount, Long operatorId,
            String operatorName, String remark)
    {
        String month = normalizeSettleMonth(settleMonth);
        BigDecimal totalAdvance = defaultZero(lyFinanceMapper.selectAdvanceTotalByUserMonth(userId, month));
        BigDecimal settledAmount = defaultZero(lyFinanceMapper.selectSettledAmountByUserMonth(userId, month));
        BigDecimal leftAmount = totalAdvance.subtract(settledAmount);
        if (leftAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            Map<String, Object> result = new HashMap<>();
            result.put("settleMonth", month);
            result.put("userId", userId);
            result.put("totalAdvance", totalAdvance);
            result.put("settledAmount", settledAmount);
            result.put("leftAmount", BigDecimal.ZERO);
            result.put("paidAmount", BigDecimal.ZERO);
            return result;
        }

        BigDecimal realPaid = (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) ? leftAmount : paidAmount;
        if (realPaid.compareTo(leftAmount) > 0)
        {
            realPaid = leftAmount;
        }

        BigDecimal leftAfterSettle = leftAmount.subtract(realPaid);
        Map<String, Object> params = new HashMap<>();
        params.put("settleMonth", month);
        params.put("userId", userId);
        params.put("totalAdvance", totalAdvance);
        params.put("paidAmount", realPaid);
        params.put("leftAmount", leftAfterSettle);
        params.put("status", leftAfterSettle.compareTo(BigDecimal.ZERO) <= 0 ? 2 : 1);
        params.put("operatorId", operatorId);
        params.put("createBy", operatorName);
        params.put("remark", StringUtils.isBlank(remark) ? "" : remark);
        params.put("snapshotJson", "{\"userId\":" + userId + ",\"settleMonth\":\"" + month + "\",\"totalAdvance\":" + totalAdvance
                + ",\"paidAmount\":" + realPaid + ",\"leftAmount\":" + leftAfterSettle + "}");
        lyFinanceMapper.insertAdvanceSettlement(params);

        Map<String, Object> result = new HashMap<>();
        result.put("settleMonth", month);
        result.put("userId", userId);
        result.put("totalAdvance", totalAdvance);
        result.put("settledAmount", settledAmount.add(realPaid));
        result.put("paidAmount", realPaid);
        result.put("leftAmount", leftAfterSettle);
        return result;
    }

    @Override
    public List<Map<String, Object>> getAdvanceSettlementList(String settleMonth)
    {
        return lyFinanceMapper.selectAdvanceSettlementList(normalizeSettleMonth(settleMonth));
    }

    @Override
    public List<Map<String, Object>> getOverdueReceivableTours()
    {
        return lyFinanceMapper.selectOverdueReceivableTours();
    }

    private String normalizeSettleMonth(String settleMonth)
    {
        return StringUtils.isBlank(settleMonth) ? DateUtils.dateTimeNow(DateUtils.YYYY_MM) : settleMonth;
    }

    private int calcBudgetWarnLevel(BigDecimal totalCost, BigDecimal budgetCost)
    {
        if (budgetCost.compareTo(BigDecimal.ZERO) <= 0)
        {
            return 0;
        }
        if (totalCost.compareTo(budgetCost) >= 0)
        {
            return 2;
        }
        if (totalCost.compareTo(budgetCost.multiply(new BigDecimal("0.9"))) >= 0)
        {
            return 1;
        }
        return 0;
    }

    private BigDecimal defaultZero(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }
}
