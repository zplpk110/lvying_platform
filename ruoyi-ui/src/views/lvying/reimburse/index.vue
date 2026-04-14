<template>
  <div class="app-container" v-loading="loading">
    <el-card class="mb12">
      <div class="wallet-num">本月我已垫付（待还）：¥{{ money(wallet.pendingAmount) }}</div>
      <div class="wallet-tip">老板端支持按员工汇总并做“本月结清/部分结清”。</div>
      <div class="wallet-tip">结算月份：{{ settleMonth }}</div>
    </el-card>
    <el-table :data="wallet.items || []">
      <el-table-column label="日期" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.occurDate, '{y}-{m}-{d} {h}:{i}') }}</template>
      </el-table-column>
      <el-table-column label="团" prop="groupName" />
      <el-table-column label="科目" prop="bizType" width="140" />
      <el-table-column label="金额" width="120">
        <template slot-scope="scope">¥{{ money(scope.row.amount) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'warning' : 'info'">
            {{ scope.row.status === 1 ? '待还' : '已处理' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-card v-if="canApprove" class="mt12">
      <div slot="header" class="card-header">
        <span>老板待付汇总（按员工）</span>
        <el-button type="primary" size="mini" @click="loadBossData">刷新</el-button>
      </div>
      <el-table :data="advanceSummary">
        <el-table-column label="员工ID" prop="userId" width="100" />
        <el-table-column label="员工" prop="userName" width="140" />
        <el-table-column label="本月垫付" width="130">
          <template slot-scope="scope">¥{{ money(scope.row.totalAdvance) }}</template>
        </el-table-column>
        <el-table-column label="已结算" width="120">
          <template slot-scope="scope">¥{{ money(scope.row.settledAmount) }}</template>
        </el-table-column>
        <el-table-column label="待还" width="120">
          <template slot-scope="scope">¥{{ money(scope.row.leftAmount) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template slot-scope="scope">
            <el-button type="success" size="mini" :disabled="Number(scope.row.leftAmount || 0) <= 0" @click="settleAll(scope.row)">本月结清</el-button>
            <el-button size="mini" :disabled="Number(scope.row.leftAmount || 0) <= 0" @click="openPartial(scope.row)">部分结清</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card v-if="canApprove" class="mt12">
      <div slot="header">结算记录</div>
      <el-table :data="settlementList">
        <el-table-column label="时间" width="170">
          <template slot-scope="scope">{{ parseTime(scope.row.settleTime, '{y}-{m}-{d} {h}:{i}') }}</template>
        </el-table-column>
        <el-table-column label="员工" prop="userName" width="130" />
        <el-table-column label="应还" width="120">
          <template slot-scope="scope">¥{{ money(scope.row.totalAdvance) }}</template>
        </el-table-column>
        <el-table-column label="实还" width="120">
          <template slot-scope="scope">¥{{ money(scope.row.paidAmount) }}</template>
        </el-table-column>
        <el-table-column label="剩余" width="120">
          <template slot-scope="scope">¥{{ money(scope.row.leftAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 2 ? 'success' : 'warning'">{{ scope.row.status === 2 ? '已结清' : '部分' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="部分结清" :visible.sync="openPartialDialog" width="420px">
      <el-form label-width="90px">
        <el-form-item label="员工">
          <el-input :value="currentUserName" disabled />
        </el-form-item>
        <el-form-item label="待还金额">
          <el-input :value="money(currentLeftAmount)" disabled />
        </el-form-item>
        <el-form-item label="本次实还">
          <el-input-number v-model="partialForm.paidAmount" :min="0.01" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="partialForm.remark" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="openPartialDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPartial">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAdvanceSettlementList, getAdvanceSummary, getMyWallet, settleAdvance } from '@/api/lvying/finance'
import { checkPermi } from '@/utils/permission'

export default {
  name: 'LvYingReimburse',
  data() {
    return {
      loading: false,
      settleMonth: '',
      wallet: {},
      canApprove: false,
      advanceSummary: [],
      settlementList: [],
      openPartialDialog: false,
      currentUserId: undefined,
      currentUserName: '',
      currentLeftAmount: 0,
      partialForm: {
        paidAmount: 0,
        remark: ''
      }
    }
  },
  created() {
    this.canApprove = checkPermi(['lvying:reimburse:approve'])
    this.loadWallet()
    if (this.canApprove) {
      this.loadBossData()
    }
  },
  methods: {
    loadWallet() {
      this.loading = true
      getMyWallet().then(walletRes => {
        this.wallet = walletRes.data || {}
        this.settleMonth = this.wallet.settleMonth || ''
      }).finally(() => {
        this.loading = false
      })
    },
    loadBossData() {
      Promise.all([
        getAdvanceSummary(this.settleMonth),
        getAdvanceSettlementList(this.settleMonth)
      ]).then(([summaryRes, listRes]) => {
        this.advanceSummary = summaryRes.data || []
        this.settlementList = listRes.data || []
      })
    },
    settleAll(row) {
      this.$modal.confirm(`确认将 ${row.userName} 在 ${this.settleMonth} 的垫资一次结清吗？`).then(() => {
        return settleAdvance({
          userId: row.userId,
          settleMonth: this.settleMonth,
          remark: '本月结清'
        })
      }).then(() => {
        this.$modal.msgSuccess('结算成功')
        this.loadWallet()
        this.loadBossData()
      })
    },
    openPartial(row) {
      this.currentUserId = row.userId
      this.currentUserName = row.userName || row.userId
      this.currentLeftAmount = Number(row.leftAmount || 0)
      this.partialForm = {
        paidAmount: this.currentLeftAmount > 0 ? this.currentLeftAmount : 0.01,
        remark: ''
      }
      this.openPartialDialog = true
    },
    submitPartial() {
      if (Number(this.partialForm.paidAmount || 0) <= 0) {
        this.$modal.msgWarning('请输入有效实还金额')
        return
      }
      settleAdvance({
        userId: this.currentUserId,
        settleMonth: this.settleMonth,
        paidAmount: this.partialForm.paidAmount,
        remark: this.partialForm.remark
      }).then(() => {
        this.$modal.msgSuccess('部分结清成功')
        this.openPartialDialog = false
        this.loadWallet()
        this.loadBossData()
      })
    },
    money(v) {
      return Number(v || 0).toFixed(2)
    }
  }
}
</script>

<style scoped lang="scss">
.mb12 { margin-bottom: 12px; }
.wallet-num { font-size: 24px; font-weight: 700; color: #e6a23c; }
.wallet-tip { margin-top: 8px; color: #909399; }
.mt12 { margin-top: 12px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
