package com.ruoyi.web.controller.lvying;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.MvpGroup;
import com.ruoyi.system.service.ILyFinanceService;

@Validated
@RestController
@RequestMapping("/lvying")
public class LyFinanceController extends BaseController
{
    @Autowired
    private ILyFinanceService lyFinanceService;

    @PreAuthorize("@ss.hasPermi('lvying:dashboard:view')")
    @GetMapping("/dashboard/overview")
    public AjaxResult dashboardOverview()
    {
        return success(lyFinanceService.getBossDashboard());
    }

    @PreAuthorize("@ss.hasPermi('lvying:tour:view')")
    @GetMapping("/group/{groupId}/detail")
    public AjaxResult tourDetail(@PathVariable Long groupId)
    {
        Map<String, Object> detail = lyFinanceService.getTourDetail(groupId);
        if (detail == null)
        {
            return error("group not found");
        }
        return success(detail);
    }

    @PreAuthorize("@ss.hasPermi('lvying:tour:edit')")
    @PostMapping("/group/open")
    public AjaxResult openGroup(@Validated @RequestBody OpenGroupRequest request)
    {
        MvpGroup group = request.toEntity();
        group.setCreateBy(getUsername());
        return success(lyFinanceService.openGroup(group));
    }

    @PreAuthorize("@ss.hasPermi('lvying:tour:edit')")
    @PostMapping("/group/{groupId}/income")
    public AjaxResult addIncome(@PathVariable Long groupId, @Validated @RequestBody IncomeRequest request)
    {
        return success(lyFinanceService.addIncome(
                groupId,
                request.getAmount(),
                request.getBizType(),
                request.getCustomerId(),
                request.getPayerName(),
                request.getRemark(),
                getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('lvying:tour:edit')")
    @PostMapping("/group/{groupId}/cost")
    public AjaxResult addExpense(@PathVariable Long groupId, @Validated @RequestBody ExpenseRequest request)
    {
        return success(lyFinanceService.addExpense(
                groupId,
                request.getAmount(),
                request.getBizType(),
                request.getAdvanceUserId(),
                request.getReceiptUrl(),
                request.getRemark(),
                getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('lvying:reimburse:view')")
    @GetMapping("/wallet/me")
    public AjaxResult myWallet()
    {
        return success(lyFinanceService.getMyWallet(getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('lvying:reimburse:approve')")
    @GetMapping("/advance/summary")
    public AjaxResult advanceSummary(String settleMonth)
    {
        return success(lyFinanceService.getAdvanceSummary(settleMonth));
    }

    @PreAuthorize("@ss.hasPermi('lvying:reimburse:approve')")
    @PostMapping("/advance/settle")
    public AjaxResult settleAdvance(@Validated @RequestBody AdvanceSettleRequest request)
    {
        return success(lyFinanceService.settleAdvance(
                request.getUserId(),
                request.getSettleMonth(),
                request.getPaidAmount(),
                getUserId(),
                getUsername(),
                request.getRemark()));
    }

    @PreAuthorize("@ss.hasPermi('lvying:reimburse:approve')")
    @GetMapping("/advance/settlement/list")
    public AjaxResult advanceSettlementList(String settleMonth)
    {
        return success(lyFinanceService.getAdvanceSettlementList(settleMonth));
    }

    @PreAuthorize("@ss.hasPermi('lvying:collection:view')")
    @GetMapping("/collection/overdue")
    public AjaxResult overdueList()
    {
        List<Map<String, Object>> list = lyFinanceService.getOverdueReceivableTours();
        return success(list);
    }

    public static class OpenGroupRequest
    {
        @NotNull(message = "groupName is required")
        @Size(min = 1, max = 64, message = "groupName length must be 1-64")
        private String groupName;

        @NotNull(message = "departDate is required")
        private java.util.Date departDate;

        private java.util.Date returnDate;

        @NotNull(message = "peopleCount is required")
        private Integer peopleCount;

        @NotNull(message = "priceMode is required")
        private Integer priceMode;

        private BigDecimal unitPrice;

        private BigDecimal totalPrice;

        @NotNull(message = "budgetCost is required")
        private BigDecimal budgetCost;

        private String remark;

        public MvpGroup toEntity()
        {
            MvpGroup group = new MvpGroup();
            group.setGroupName(groupName);
            group.setDepartDate(departDate);
            group.setReturnDate(returnDate);
            group.setPeopleCount(peopleCount);
            group.setPriceMode(priceMode);
            group.setUnitPrice(unitPrice);
            group.setTotalPrice(totalPrice);
            group.setBudgetCost(budgetCost);
            group.setStatus(1);
            group.setRemark(remark);
            return group;
        }

        public String getGroupName()
        {
            return groupName;
        }

        public void setGroupName(String groupName)
        {
            this.groupName = groupName;
        }

        public java.util.Date getDepartDate()
        {
            return departDate;
        }

        public void setDepartDate(java.util.Date departDate)
        {
            this.departDate = departDate;
        }

        public java.util.Date getReturnDate()
        {
            return returnDate;
        }

        public void setReturnDate(java.util.Date returnDate)
        {
            this.returnDate = returnDate;
        }

        public Integer getPeopleCount()
        {
            return peopleCount;
        }

        public void setPeopleCount(Integer peopleCount)
        {
            this.peopleCount = peopleCount;
        }

        public Integer getPriceMode()
        {
            return priceMode;
        }

        public void setPriceMode(Integer priceMode)
        {
            this.priceMode = priceMode;
        }

        public BigDecimal getUnitPrice()
        {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice)
        {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getTotalPrice()
        {
            return totalPrice;
        }

        public void setTotalPrice(BigDecimal totalPrice)
        {
            this.totalPrice = totalPrice;
        }

        public BigDecimal getBudgetCost()
        {
            return budgetCost;
        }

        public void setBudgetCost(BigDecimal budgetCost)
        {
            this.budgetCost = budgetCost;
        }

        public String getRemark()
        {
            return remark;
        }

        public void setRemark(String remark)
        {
            this.remark = remark;
        }
    }

    public static class IncomeRequest
    {
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        private BigDecimal amount;

        private String bizType;

        private Long customerId;

        private String payerName;

        private String remark;

        public BigDecimal getAmount()
        {
            return amount;
        }

        public void setAmount(BigDecimal amount)
        {
            this.amount = amount;
        }

        public String getBizType()
        {
            return bizType;
        }

        public void setBizType(String bizType)
        {
            this.bizType = bizType;
        }

        public Long getCustomerId()
        {
            return customerId;
        }

        public void setCustomerId(Long customerId)
        {
            this.customerId = customerId;
        }

        public String getPayerName()
        {
            return payerName;
        }

        public void setPayerName(String payerName)
        {
            this.payerName = payerName;
        }

        public String getRemark()
        {
            return remark;
        }

        public void setRemark(String remark)
        {
            this.remark = remark;
        }
    }

    public static class ExpenseRequest
    {
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        private BigDecimal amount;

        private String bizType;

        private Long advanceUserId;

        private String receiptUrl;

        private String remark;

        public BigDecimal getAmount()
        {
            return amount;
        }

        public void setAmount(BigDecimal amount)
        {
            this.amount = amount;
        }

        public String getBizType()
        {
            return bizType;
        }

        public void setBizType(String bizType)
        {
            this.bizType = bizType;
        }

        public Long getAdvanceUserId()
        {
            return advanceUserId;
        }

        public void setAdvanceUserId(Long advanceUserId)
        {
            this.advanceUserId = advanceUserId;
        }

        public String getReceiptUrl()
        {
            return receiptUrl;
        }

        public void setReceiptUrl(String receiptUrl)
        {
            this.receiptUrl = receiptUrl;
        }

        public String getRemark()
        {
            return remark;
        }

        public void setRemark(String remark)
        {
            this.remark = remark;
        }
    }

    public static class AdvanceSettleRequest
    {
        @NotNull(message = "userId is required")
        private Long userId;

        private String settleMonth;

        @DecimalMin(value = "0.01", message = "paidAmount must be greater than 0")
        private BigDecimal paidAmount;

        private String remark;

        public Long getUserId()
        {
            return userId;
        }

        public void setUserId(Long userId)
        {
            this.userId = userId;
        }

        public String getSettleMonth()
        {
            return settleMonth;
        }

        public void setSettleMonth(String settleMonth)
        {
            this.settleMonth = settleMonth;
        }

        public BigDecimal getPaidAmount()
        {
            return paidAmount;
        }

        public void setPaidAmount(BigDecimal paidAmount)
        {
            this.paidAmount = paidAmount;
        }

        public String getRemark()
        {
            return remark;
        }

        public void setRemark(String remark)
        {
            this.remark = remark;
        }
    }
}
