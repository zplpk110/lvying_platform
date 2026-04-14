package com.ruoyi.web.controller.lvying;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
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
    @GetMapping("/tour/{tourId}/detail")
    public AjaxResult tourDetail(@PathVariable Long tourId)
    {
        Map<String, Object> detail = lyFinanceService.getTourDetail(tourId);
        if (detail == null)
        {
            return error("团期不存在");
        }
        return success(detail);
    }

    @PreAuthorize("@ss.hasPermi('lvying:tour:edit')")
    @PostMapping("/tour/{tourId}/income")
    public AjaxResult addIncome(@PathVariable Long tourId, @Validated @RequestBody IncomeRequest request)
    {
        return success(lyFinanceService.addIncome(tourId, request.getAmount(), request.getIncomeType(), request.getRemark()));
    }

    @PreAuthorize("@ss.hasPermi('lvying:tour:edit')")
    @PostMapping("/tour/{tourId}/expense")
    public AjaxResult addExpense(@PathVariable Long tourId,
            @RequestParam(defaultValue = "false") boolean forceConfirm,
            @Validated @RequestBody ExpenseRequest request)
    {
        Map<String, Object> data = lyFinanceService.addExpense(
                tourId,
                request.getAmount(),
                request.getCategory(),
                request.getPaymentMethod(),
                request.getAdvanceUserName(),
                request.getReceiptUrl(),
                request.getRemark(),
                forceConfirm);
        if (data == null)
        {
            return error("团期不存在");
        }
        if (Boolean.TRUE.equals(data.get("needOwnerConfirm")))
        {
            return success(data).put("msg", StringUtils.nvl(data.get("message"), "需要老板确认"));
        }
        return success(data);
    }

    @PreAuthorize("@ss.hasPermi('lvying:reimburse:view')")
    @GetMapping("/reimbursement/my-wallet")
    public AjaxResult myWallet()
    {
        return success(lyFinanceService.getMyWallet(getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('lvying:reimburse:approve')")
    @GetMapping("/reimbursement/approvals")
    public AjaxResult reimbursementApprovals()
    {
        return success(lyFinanceService.getReimbursementApprovals());
    }

    @PreAuthorize("@ss.hasPermi('lvying:reimburse:approve')")
    @PostMapping("/reimbursement/{costId}/approve")
    public AjaxResult approveReimbursement(@PathVariable Long costId, @RequestBody(required = false) ApprovalRequest request)
    {
        String remark = request == null ? "" : request.getRemark();
        return toAjax(lyFinanceService.approveReimbursement(costId, remark, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('lvying:reimburse:approve')")
    @PostMapping("/reimbursement/{costId}/reject")
    public AjaxResult rejectReimbursement(@PathVariable Long costId, @RequestBody(required = false) ApprovalRequest request)
    {
        String remark = request == null ? "" : request.getRemark();
        return toAjax(lyFinanceService.rejectReimbursement(costId, remark, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('lvying:collection:view')")
    @GetMapping("/collection/overdue")
    public AjaxResult overdueList()
    {
        List<Map<String, Object>> list = lyFinanceService.getOverdueReceivableTours();
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('lvying:collection:send')")
    @PostMapping("/collection/send-batch")
    public AjaxResult sendBatchReminder()
    {
        List<Map<String, Object>> list = lyFinanceService.getOverdueReceivableTours();
        return success("已触发催收提醒：" + list.size() + "条");
    }

    public static class IncomeRequest
    {
        @NotNull(message = "金额不能为空")
        private BigDecimal amount;

        private String incomeType;

        private String remark;

        public BigDecimal getAmount()
        {
            return amount;
        }

        public void setAmount(BigDecimal amount)
        {
            this.amount = amount;
        }

        public String getIncomeType()
        {
            return incomeType;
        }

        public void setIncomeType(String incomeType)
        {
            this.incomeType = incomeType;
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
        @NotNull(message = "金额不能为空")
        private BigDecimal amount;

        private String category;

        private String paymentMethod;

        private String advanceUserName;

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

        public String getCategory()
        {
            return category;
        }

        public void setCategory(String category)
        {
            this.category = category;
        }

        public String getPaymentMethod()
        {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod)
        {
            this.paymentMethod = paymentMethod;
        }

        public String getAdvanceUserName()
        {
            return advanceUserName;
        }

        public void setAdvanceUserName(String advanceUserName)
        {
            this.advanceUserName = advanceUserName;
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

    public static class ApprovalRequest
    {
        private String remark;

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
