package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * MVP 收支流水对象 mvp_finance_record
 */
public class MvpFinanceRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long groupId;

    private Long customerId;

    /**
     * 流水类型 1收入 2成本
     */
    private Integer recordType;

    private String bizType;

    private BigDecimal amount;

    private Date occurDate;

    private String payerName;

    private Long advanceUserId;

    private String voucherUrl;

    private BigDecimal ocrAmount;

    /**
     * 状态 1有效 9作废
     */
    private Integer status;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getGroupId()
    {
        return groupId;
    }

    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public Integer getRecordType()
    {
        return recordType;
    }

    public void setRecordType(Integer recordType)
    {
        this.recordType = recordType;
    }

    public String getBizType()
    {
        return bizType;
    }

    public void setBizType(String bizType)
    {
        this.bizType = bizType;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public Date getOccurDate()
    {
        return occurDate;
    }

    public void setOccurDate(Date occurDate)
    {
        this.occurDate = occurDate;
    }

    public String getPayerName()
    {
        return payerName;
    }

    public void setPayerName(String payerName)
    {
        this.payerName = payerName;
    }

    public Long getAdvanceUserId()
    {
        return advanceUserId;
    }

    public void setAdvanceUserId(Long advanceUserId)
    {
        this.advanceUserId = advanceUserId;
    }

    public String getVoucherUrl()
    {
        return voucherUrl;
    }

    public void setVoucherUrl(String voucherUrl)
    {
        this.voucherUrl = voucherUrl;
    }

    public BigDecimal getOcrAmount()
    {
        return ocrAmount;
    }

    public void setOcrAmount(BigDecimal ocrAmount)
    {
        this.ocrAmount = ocrAmount;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }
}
