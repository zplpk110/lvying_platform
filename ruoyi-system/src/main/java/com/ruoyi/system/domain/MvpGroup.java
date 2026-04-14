package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * MVP 团主表对象 mvp_group
 */
public class MvpGroup extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    private String groupName;

    private Date departDate;

    private Date returnDate;

    private Integer peopleCount;

    /**
     * 计价方式 1人均 2总包
     */
    private Integer priceMode;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    private BigDecimal budgetCost;

    /**
     * 状态 1进行中 2已结团
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

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public Date getDepartDate()
    {
        return departDate;
    }

    public void setDepartDate(Date departDate)
    {
        this.departDate = departDate;
    }

    public Date getReturnDate()
    {
        return returnDate;
    }

    public void setReturnDate(Date returnDate)
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

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }
}
