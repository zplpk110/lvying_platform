<template>
  <div class="app-container" v-loading="loading">
    <el-card class="mb12">
      <div class="header-line">
        团号 #{{ detail.tourNo }} | {{ detail.tourName }} | 状态：{{ detail.status }}
      </div>
    </el-card>

    <el-row :gutter="12">
      <el-col :span="8">
        <el-card class="green">
          <div slot="header">收银台</div>
          <div>总应收：¥{{ money(detail.totalReceivable) }}</div>
          <div>实收现金：¥{{ money(detail.actualIncome) }}</div>
          <div>尚欠尾款：¥{{ money((detail.totalReceivable || 0) - (detail.actualIncome || 0)) }}</div>
          <el-button size="mini" type="success" @click="openIncome = true" style="margin-top:10px;">记一笔收款</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="red">
          <div slot="header">成本夹</div>
          <div>预算红线：¥{{ money(detail.budgetLimit) }}</div>
          <div>当前已花费：¥{{ money(detail.paidCost) }}</div>
          <div>待支付/待报销：¥{{ money(detail.pendingCost) }}</div>
          <div>当前总成本预估：¥{{ money(detail.estimatedTotalCost) }}</div>
          <div>实时利润测算：¥{{ money(detail.estimatedProfit) }}</div>
          <el-button size="mini" type="danger" @click="openExpense = true" style="margin-top:10px;">记一笔支出/报销</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div slot="header">利润结算卡</div>
          <div>提成比例：{{ percent(detail.commissionRate) }}</div>
          <div>预估提成金额：¥{{ money(detail.estimatedCommission) }}</div>
          <el-button size="mini" type="primary" style="margin-top:10px;" @click="$modal.msgSuccess('已发起封团结算申请（MVP演示）')">申请封团结算</el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog title="记一笔收款" :visible.sync="openIncome" width="420px">
      <el-form label-width="90px">
        <el-form-item label="金额">
          <el-input-number v-model="incomeForm.amount" :min="0" :precision="2" style="width:100%;" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="incomeForm.incomeType" style="width:100%;">
            <el-option label="定金" value="deposit" />
            <el-option label="尾款" value="final" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="incomeForm.remark" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="openIncome = false">取消</el-button>
        <el-button type="primary" @click="submitIncome">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog title="记一笔支出/报销" :visible.sync="openExpense" width="460px">
      <el-form label-width="105px">
        <el-form-item label="票据URL">
          <el-input v-model="expenseForm.receiptUrl" placeholder="MVP先填写票据链接" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="expenseForm.amount" :min="0" :precision="2" style="width:100%;" />
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="expenseForm.category" style="width:100%;">
            <el-option label="住宿" value="hotel" />
            <el-option label="餐饮" value="meal" />
            <el-option label="门票" value="ticket" />
            <el-option label="交通" value="transport" />
            <el-option label="司导费" value="guide" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="expenseForm.paymentMethod" style="width:100%;">
            <el-option label="公账支付" value="PUBLIC" />
            <el-option label="员工垫付" value="ADVANCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="垫付人" v-if="expenseForm.paymentMethod === 'ADVANCE'">
          <el-input v-model="expenseForm.advanceUserName" placeholder="例如：张三" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="openExpense = false">取消</el-button>
        <el-button type="primary" @click="submitExpense(false)">提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { addTourExpense, addTourIncome, getTourDetail } from '@/api/lvying/finance'

export default {
  name: 'LvYingTourDetail',
  data() {
    return {
      loading: false,
      detail: {},
      openIncome: false,
      openExpense: false,
      incomeForm: { amount: 0, incomeType: 'deposit', remark: '' },
      expenseForm: {
        receiptUrl: '',
        amount: 0,
        category: 'hotel',
        paymentMethod: 'PUBLIC',
        advanceUserName: '',
        remark: ''
      }
    }
  },
  computed: {
    tourId() {
      return this.$route.params.tourId
    }
  },
  created() {
    this.loadDetail()
  },
  methods: {
    loadDetail() {
      this.loading = true
      getTourDetail(this.tourId).then(res => {
        this.detail = res.data || {}
      }).finally(() => {
        this.loading = false
      })
    },
    submitIncome() {
      addTourIncome(this.tourId, this.incomeForm).then(() => {
        this.$modal.msgSuccess('收款记录成功')
        this.openIncome = false
        this.loadDetail()
      })
    },
    submitExpense(forceConfirm) {
      addTourExpense(this.tourId, this.expenseForm, forceConfirm).then(res => {
        if (res.data && res.data.needOwnerConfirm) {
          this.$modal.confirm('该支出将使成本超过预算红线，需老板确认，是否继续提交？').then(() => {
            this.submitExpense(true)
          })
          return
        }
        this.$modal.msgSuccess('支出记录成功')
        this.openExpense = false
        this.loadDetail()
      })
    },
    money(v) {
      return Number(v || 0).toFixed(2)
    },
    percent(v) {
      return (Number(v || 0) * 100).toFixed(0) + '%'
    }
  }
}
</script>

<style scoped lang="scss">
.mb12 { margin-bottom: 12px; }
.header-line { font-size: 16px; font-weight: 700; }
.green { border-top: 3px solid #67c23a; }
.red { border-top: 3px solid #f56c6c; }
</style>
