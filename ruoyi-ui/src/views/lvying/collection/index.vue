<template>
  <div class="app-container" v-loading="loading">
    <el-card class="mb12">
      <div slot="header">尾款逾期列表</div>
    </el-card>

    <el-table :data="list">
      <el-table-column label="团ID" prop="groupId" width="100" />
      <el-table-column label="团名" prop="groupName" />
      <el-table-column label="出团日期" width="120">
        <template slot-scope="scope">
          {{ parseTime(scope.row.departDate, '{y}-{m}-{d}') }}
        </template>
      </el-table-column>
      <el-table-column label="未付人数" prop="unpaidCount" width="100" />
      <el-table-column label="未收尾款" width="150">
        <template slot-scope="scope">¥{{ money(scope.row.unpaidAmount) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template slot-scope="scope">
          <el-button type="text" @click="copyText(scope.row)">复制催款话术</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { getOverdueList } from '@/api/lvying/finance'

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
    copyText(row) {
      const text = `您好，${row.groupName} 还有尾款未结清，当前未付 ${row.unpaidCount} 人，共 ${this.money(row.unpaidAmount)} 元，请在出团前完成支付。谢谢。`
      if (navigator && navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(() => {
          this.$modal.msgSuccess('催款话术已复制')
        })
      } else {
        this.$modal.msgWarning('当前浏览器不支持自动复制，请手动复制')
      }
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
