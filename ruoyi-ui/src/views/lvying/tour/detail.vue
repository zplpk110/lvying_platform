<template>
  <div class="app-container" v-loading="loading">
    <el-card class="mb12">
      <div class="header-line">
        团名：{{ detail.groupName }} | 状态：{{ detail.status === 1 ? '进行中' : '已结团' }}
      </div>
    </el-card>

    <el-row :gutter="12">
      <el-col :span="8">
        <el-card class="green">
          <div slot="header">收银台</div>
          <div>总应收：¥{{ money(detail.shouldReceivable) }}</div>
          <div>实收现金：¥{{ money(detail.actualIncome) }}</div>
          <div>尚欠尾款：¥{{ money((detail.shouldReceivable || 0) - (detail.actualIncome || 0)) }}</div>
          <el-button size="mini" type="success" @click="openIncome = true" style="margin-top:10px;">记一笔收款</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="red">
          <div slot="header">成本夹</div>
          <div>预算红线：¥{{ money(detail.budgetCost) }}</div>
          <div>当前总成本：¥{{ money(detail.totalCost) }}</div>
          <div>实时利润测算：¥{{ money(detail.estimatedProfit) }}</div>
          <div style="margin-top: 6px;">
            <el-tag v-if="detail.budgetWarnLevel === 2" type="danger">成本红灯</el-tag>
            <el-tag v-else-if="detail.budgetWarnLevel === 1" type="warning">成本黄灯</el-tag>
            <el-tag v-else type="success">成本正常</el-tag>
          </div>
          <el-button size="mini" type="danger" @click="openExpense = true" style="margin-top:10px;">记一笔成本</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div slot="header">现金口径</div>
          <div>现金利润：¥{{ money(detail.cashProfit) }}</div>
          <div style="margin-top: 8px;">出团：{{ parseTime(detail.departDate, '{y}-{m}-{d}') }}</div>
          <div>返程：{{ parseTime(detail.returnDate, '{y}-{m}-{d}') }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="mb12" style="margin-top: 12px;">
      <div slot="header">游客尾款清单</div>
      <el-table :data="detail.customers || []" size="small">
        <el-table-column label="姓名" prop="customerName" width="100" />
        <el-table-column label="手机号" prop="phone" width="140" />
        <el-table-column label="应收" width="110">
          <template slot-scope="scope">¥{{ money(scope.row.shouldPay) }}</template>
        </el-table-column>
        <el-table-column label="已收" width="110">
          <template slot-scope="scope">¥{{ money(scope.row.paidAmount) }}</template>
        </el-table-column>
        <el-table-column label="未收" width="110">
          <template slot-scope="scope">¥{{ money(scope.row.unpaidAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.paymentStatus === 2" type="success">已付</el-tag>
            <el-tag v-else-if="scope.row.paymentStatus === 1" type="warning">部分</el-tag>
            <el-tag v-else type="danger">未付</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card>
      <div slot="header">收支流水</div>
      <el-table :data="detail.records || []" size="small">
        <el-table-column label="时间" width="160">
          <template slot-scope="scope">{{ parseTime(scope.row.occurDate, '{y}-{m}-{d} {h}:{i}') }}</template>
        </el-table-column>
        <el-table-column label="类型" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.recordType === 1 ? 'success' : 'danger'">
              {{ scope.row.recordType === 1 ? '收入' : '成本' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="科目" prop="bizType" />
        <el-table-column label="金额" width="120">
          <template slot-scope="scope">¥{{ money(scope.row.amount) }}</template>
        </el-table-column>
        <el-table-column label="垫付人ID" prop="advanceUserId" width="110" />
        <el-table-column label="备注" prop="remark" />
      </el-table>
    </el-card>

    <el-dialog title="记一笔收款" :visible.sync="openIncome" width="420px">
      <el-form label-width="90px">
        <el-form-item label="金额">
          <el-input-number v-model="incomeForm.amount" :min="0" :precision="2" style="width:100%;" />
        </el-form-item>
        <el-form-item label="科目">
          <el-select v-model="incomeForm.bizType" style="width:100%;">
            <el-option label="定金" value="定金" />
            <el-option label="尾款" value="尾款" />
            <el-option label="其他收入" value="OTHER_INCOME" />
          </el-select>
        </el-form-item>
        <el-form-item label="游客">
          <el-select v-model="incomeForm.customerId" clearable placeholder="可选，关联后自动更新已收">
            <el-option v-for="item in detail.customers || []" :key="item.customerId" :label="item.customerName + ' (' + item.phone + ')'" :value="item.customerId" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款人">
          <el-input v-model="incomeForm.payerName" />
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

    <el-dialog title="记一笔成本" :visible.sync="openExpense" width="460px">
      <el-form label-width="95px">
        <el-form-item label="票据URL">
          <el-input v-model="expenseForm.receiptUrl" placeholder="MVP先填写票据链接" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="expenseForm.amount" :min="0" :precision="2" style="width:100%;" />
        </el-form-item>
        <el-form-item label="科目">
          <el-select v-model="expenseForm.bizType" style="width:100%;">
            <el-option label="车费" value="车费" />
            <el-option label="住宿" value="住宿" />
            <el-option label="餐费" value="餐费" />
            <el-option label="门票" value="门票" />
            <el-option label="导游费" value="导游费" />
            <el-option label="杂费" value="OTHER_COST" />
          </el-select>
        </el-form-item>
        <el-form-item label="垫付人ID">
          <el-input-number v-model="expenseForm.advanceUserId" :min="1" :precision="0" style="width:100%;" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="openExpense = false">取消</el-button>
        <el-button type="primary" @click="submitExpense">提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { addGroupCost, addGroupIncome, getGroupDetail } from '@/api/lvying/finance'

export default {
  name: 'LvYingTourDetail',
  data() {
    return {
      loading: false,
      detail: {},
      openIncome: false,
      openExpense: false,
      incomeForm: { amount: 0, bizType: '定金', customerId: undefined, payerName: '', remark: '' },
      expenseForm: {
        receiptUrl: '',
        amount: 0,
        bizType: '车费',
        advanceUserId: undefined,
        remark: ''
      }
    }
  },
  computed: {
    groupId() {
      return this.$route.params.tourId
    }
  },
  created() {
    this.loadDetail()
  },
  methods: {
    loadDetail() {
      this.loading = true
      getGroupDetail(this.groupId).then(res => {
        this.detail = res.data || {}
      }).finally(() => {
        this.loading = false
      })
    },
    submitIncome() {
      addGroupIncome(this.groupId, this.incomeForm).then(() => {
        this.$modal.msgSuccess('收款记录成功')
        this.openIncome = false
        this.loadDetail()
      })
    },
    submitExpense() {
      addGroupCost(this.groupId, this.expenseForm).then(() => {
        this.$modal.msgSuccess('支出记录成功')
        this.openExpense = false
        this.loadDetail()
      })
    },
    money(v) {
      return Number(v || 0).toFixed(2)
    },
  }
}
</script>

<style scoped lang="scss">
.mb12 { margin-bottom: 12px; }
.header-line { font-size: 16px; font-weight: 700; }
.green { border-top: 3px solid #67c23a; }
.red { border-top: 3px solid #f56c6c; }
</style>
