<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
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
    showToast('已生成清单')
  } catch (e) {
    showToast('导出失败')
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
  <div class="page">
    <van-nav-bar title="批量转账 / 代发文件" left-arrow @click-left="router.back()" fixed placeholder />
    <van-cell-group inset>
      <van-field v-model="year" label="年" type="digit" />
      <van-field v-model="month" label="月" type="digit" />
    </van-cell-group>
    <div class="btn">
      <van-button type="primary" block round :loading="loading" @click="exportCsv">生成银行代发 CSV</van-button>
    </div>
    <van-cell-group v-if="result?.rows?.length" inset title="汇总">
      <van-cell v-for="(r, i) in result.rows" :key="i" :title="r.staffName" :label="`${r.bankName} 尾号${r.accountLast4}`" :value="`¥${r.totalAmount}`" />
    </van-cell-group>
    <div class="btn" v-if="result?.csv">
      <van-button block round @click="download">下载 CSV上传网银</van-button>
    </div>
  </div>
</template>

<style scoped>
.page { padding-bottom: 32px; }
.btn { margin: 16px; }
</style>
