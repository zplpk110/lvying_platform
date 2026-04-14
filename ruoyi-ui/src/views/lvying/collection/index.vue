<template>
  <div class="app-container" v-loading="loading">
    <el-card class="mb12">
      <div slot="header">尾款逾期列表</div>
      <el-button type="danger" size="mini" @click="sendAll">群发提醒</el-button>
    </el-card>

    <el-table :data="list">
      <el-table-column label="团号" prop="tourNo" width="120" />
      <el-table-column label="团名" prop="tourName" />
      <el-table-column label="出团日期" width="120">
        <template slot-scope="scope">
          {{ parseTime(scope.row.departDate, '{y}-{m}-{d}') }}
        </template>
      </el-table-column>
      <el-table-column label="负责销售" prop="salesUserName" width="120" />
      <el-table-column label="手机号" prop="customerPhone" width="140" />
      <el-table-column label="未收尾款" width="140">
        <template slot-scope="scope">¥{{ money(scope.row.finalReceivable) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template slot-scope="scope">
          <el-button type="text" @click="sendOne(scope.row)">发送提醒</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { getOverdueList, sendBatchReminder } from '@/api/lvying/finance'

export default {
  name: 'LvYingCollection',
  data() {
    return {
      loading: false,
      list: []
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      getOverdueList().then(res => {
        this.list = res.data || []
      }).finally(() => {
        this.loading = false
      })
    },
    sendAll() {
      sendBatchReminder().then(res => {
        this.$modal.msgSuccess(res.data || res.msg || '已触发催收提醒')
      })
    },
    sendOne(row) {
      this.$modal.msgSuccess('已向 ' + row.customerPhone + ' 发送提醒（MVP模拟）')
    },
    money(v) {
      return Number(v || 0).toFixed(2)
    }
  }
}
</script>

<style scoped>
.mb12 { margin-bottom: 12px; }
</style>
