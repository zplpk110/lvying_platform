<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const year = ref(new Date().getFullYear())
const month = ref(new Date().getMonth() + 1)
const result = ref(null)
const loading = ref(false)

async function exportCsv() {
  if (!auth.isBoss) return
  loading.value = true
  try {
    const { data } = await http.get('/reimbursements/batch-export', {
      params: { year: year.value, month: month.value },
    })
    result.value = data
    ElMessage.success('已生成清单')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

function download() {
  if (!result.value?.csv) return
  const blob = new Blob([result.value.csv], { type: 'text/csv;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `lvying-payroll-${result.value.period}.csv`
  a.click()
}
</script>

<template>
  <div v-if="auth.isBoss">
    <div class="toolbar">
      <span class="h">月结代发 · 银行 CSV</span>
      <el-button link type="primary" @click="router.push({ name: 'reimburse' })">返回审批</el-button>
    </div>
    <el-card shadow="never" class="mb">
      <el-form inline>
        <el-form-item label="年">
          <el-input-number v-model="year" :min="2000" :max="2100" />
        </el-form-item>
        <el-form-item label="月">
          <el-input-number v-model="month" :min="1" :max="12" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="exportCsv">生成代发 CSV</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card v-if="result?.rows?.length" shadow="never">
      <template #header>汇总</template>
      <el-table :data="result.rows" stripe>
        <el-table-column prop="staffName" label="姓名" />
        <el-table-column prop="totalAmount" label="金额" />
        <el-table-column prop="bankName" label="开户行" />
        <el-table-column prop="accountLast4" label="尾号" />
        <el-table-column prop="phone" label="手机" />
      </el-table>
      <el-button v-if="result?.csv" type="success" class="mt" @click="download">下载 CSV</el-button>
    </el-card>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.h {
  font-size: 16px;
  font-weight: 600;
}
.mb {
  margin-bottom: 16px;
}
.mt {
  margin-top: 16px;
}
</style>
