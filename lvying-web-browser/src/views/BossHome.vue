<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const router = useRouter()
const data = ref(null)
const loading = ref(true)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const { data: d } = await http.get('/dashboard/boss')
    data.value = d
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
 }
}

function money(s) {
  if (s == null) return '—'
  const n = Number(s)
  return `¥${n.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })}`
}
</script>

<template>
  <div v-loading="loading">
    <template v-if="data">
      <el-alert
        v-if="data.urgent.pendingReimbursementApprovals || data.urgent.tailDueBeforeDeparture || data.urgent.marginAlertTours"
        type="warning"
        show-icon
        :closable="false"
        class="mb"
        :title="`今日待办：审批 ${data.urgent.pendingReimbursementApprovals} 笔 · 尾款预警团 ${data.urgent.tailDueBeforeDeparture} · 毛利预警 ${data.urgent.marginAlertTours}`"
      />

      <el-row :gutter="16" class="mb">
        <el-col :span="8">
          <el-card shadow="never">
            <div class="kpi-label">可用余额</div>
            <div class="kpi-num primary">{{ money(data.fundPool.availableBalance) }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <div class="kpi-label">在途欠员工垫款</div>
            <div class="kpi-num">{{ money(data.fundPool.staffAdvanceOutstanding) }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <div class="kpi-label">预计本月净利</div>
            <div class="kpi-num success">{{ money(data.fundPool.estimatedMonthNetProfit) }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never">
        <template #header>
          <div class="card-head">
            <span>进行中团队</span>
            <el-button type="primary" link @click="router.push({ name: 'tours' })">查看全部</el-button>
          </div>
        </template>
        <el-table :data="data.inProgressBoard" stripe @row-click="(row) => router.push({ name: 'tour-detail', params: { id: row.id } })">
          <el-table-column prop="tourCode" label="团号" width="100" />
          <el-table-column prop="name" label="行程" min-width="160" />
          <el-table-column prop="departureDate" label="出团日" width="120" />
          <el-table-column prop="incomeExpenseRatio" label="收/支比" width="100" />
          <el-table-column label="净利预估" width="140">
            <template #default="{ row }">
              <span class="success">{{ money(row.netProfitEstimate) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>
  </div>
</template>

<style scoped>
.mb {
  margin-bottom: 16px;
}
.kpi-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.kpi-num {
  font-size: 22px;
  font-weight: 600;
  margin-top: 8px;
}
.kpi-num.primary {
  color: var(--el-color-primary);
}
.kpi-num.success {
  color: var(--el-color-success);
}
.success {
  color: var(--el-color-success);
}
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
</style>
