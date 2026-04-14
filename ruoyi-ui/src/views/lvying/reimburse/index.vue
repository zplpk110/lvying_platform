<template>
  <div class="app-container" v-loading="loading">
    <el-tabs v-model="active">
      <el-tab-pane label="我的钱包" name="wallet">
        <el-card class="mb12">
          <div class="wallet-num">当前待公司还款总额：¥{{ money(wallet.pendingAmount) }}</div>
        </el-card>
        <el-table :data="wallet.items || []">
          <el-table-column label="日期" width="120">
            <template slot-scope="scope">{{ parseTime(scope.row.occurDate, '{y}-{m}-{d}') }}</template>
          </el-table-column>
          <el-table-column label="团号" prop="tourNo" width="130" />
          <el-table-column label="类别" prop="category" />
          <el-table-column label="金额" width="120">
            <template slot-scope="scope">¥{{ money(scope.row.amount) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="140">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 'PAID' ? 'success' : (scope.row.status === 'REJECTED' ? 'info' : 'warning')">
                {{ statusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="报销审批" name="approve">
        <el-table :data="approvals">
          <el-table-column label="团号" prop="tourNo" width="120" />
          <el-table-column label="业务员" prop="advanceUserName" width="120" />
          <el-table-column label="垫付明细" prop="category" />
          <el-table-column label="金额" width="120">
            <template slot-scope="scope">¥{{ money(scope.row.amount) }}</template>
          </el-table-column>
          <el-table-column label="票据" width="120">
            <template slot-scope="scope">
              <el-button type="text" @click="preview(scope.row.receiptUrl)">查看</el-button>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220">
            <template slot-scope="scope">
              <el-button size="mini" type="success" @click="approve(scope.row.costId)">一键通过</el-button>
              <el-button size="mini" @click="reject(scope.row.costId)">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import {
  approveReimbursement,
  getMyWallet,
  getReimbursementApprovals,
  rejectReimbursement
} from '@/api/lvying/finance'

export default {
  name: 'LvYingReimburse',
  data() {
    return {
      loading: false,
      active: 'wallet',
      wallet: {},
      approvals: []
    }
  },
  created() {
    this.loadAll()
  },
  methods: {
    loadAll() {
      this.loading = true
      Promise.all([getMyWallet(), getReimbursementApprovals()]).then(([walletRes, approvalRes]) => {
        this.wallet = walletRes.data || {}
        this.approvals = approvalRes.data || []
      }).finally(() => {
        this.loading = false
      })
    },
    approve(costId) {
      approveReimbursement(costId, '').then(() => {
        this.$modal.msgSuccess('已审批通过')
        this.loadAll()
      })
    },
    reject(costId) {
      rejectReimbursement(costId, '票据不完整').then(() => {
        this.$modal.msgSuccess('已驳回')
        this.loadAll()
      })
    },
    preview(url) {
      if (!url) {
        this.$modal.msgWarning('暂无票据链接')
        return
      }
      window.open(url, '_blank')
    },
    statusText(status) {
      if (status === 'PAID') return '已到账'
      if (status === 'REJECTED') return '已驳回'
      return '待打款'
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
</style>
