<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const router = useRouter()
const data = ref(null)
const loading = ref(true)

function categoryLabel(c) {
  const m = {
    LODGING: '住宿',
    DINING: '餐饮',
    TICKET: '门票',
    TRANSPORT: '交通',
    DRIVER_GUIDE: '司导费',
    OTHER: '其他',
  }
  return m[c] || c
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const { data: d } = await http.get('/dashboard/staff')
    data.value = d
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function money(s) {
  const n = Number(s)
  return `¥${n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}
</script>

<template>
  <div v-loading="loading">
    <template v-if="data">
      <el-card shadow="never" class="mb">
        <div class="kpi-label">待公司还款</div>
        <div class="kpi-num">{{ money(data.pendingRepaymentTotal) }}</div>
      </el-card>

      <el-card shadow="never">
        <template #header>近期垫付</template>
        <el-table :data="data.recentLines" stripe>
          <el-table-column prop="date" label="日期" width="110" />
          <el-table-column prop="tourCode" label="团号" width="100" />
          <el-table-column prop="tourName" label="行程" min-width="140" />
          <el-table-column label="类别" width="100">
            <template #default="{ row }">{{ categoryLabel(row.category) }}</template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="{ row }">{{ money(row.amount) }}</template>
          </el-table-column>
          <el-table-column prop="displayStatus" label="状态" width="100" />
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
  font-size: 24px;
  font-weight: 600;
  margin-top: 8px;
  color: var(--el-color-primary);
}
</style>
