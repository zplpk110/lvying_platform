<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const data = ref(null)
const loading = ref(true)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const { data: d } = await http.get('/dashboard/boss')
    data.value = d
  } catch (e) {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

function money(s) {
  if (s == null) return '—'
  const n = Number(s)
  return `¥${n.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })}`
}

function logout() {
  auth.clear()
  router.replace({ name: 'login' })
}
</script>

<template>
  <div class="page" v-if="data">
    <van-nav-bar title="经营仪表盘" fixed placeholder>
      <template #right>
        <span class="link" @click="logout">退出</span>
      </template>
    </van-nav-bar>

    <van-notice-bar v-if="data.urgent.pendingReimbursementApprovals || data.urgent.tailDueBeforeDeparture || data.urgent.marginAlertTours"
      color="#ee0a24" background="#ffe7e7" left-icon="warning-o" mode="closeable">
      今日待办：审批 {{ data.urgent.pendingReimbursementApprovals }} 笔 ·
      尾款预警团 {{ data.urgent.tailDueBeforeDeparture }} ·
      毛利预警 {{ data.urgent.marginAlertTours }}
    </van-notice-bar>

    <div class="hero">
      <div class="hero-label">可用余额（能动用的钱）</div>
      <div class="hero-num">{{ money(data.fundPool.availableBalance) }}</div>
      <div class="hero-sub">在途欠员工垫款 {{ money(data.fundPool.staffAdvanceOutstanding) }}</div>
      <div class="hero-sub">预计本月净利 {{ money(data.fundPool.estimatedMonthNetProfit) }}</div>
    </div>

    <van-cell-group inset title="进行中团队" class="mt">
      <van-cell
        v-for="t in data.inProgressBoard"
        :key="t.id"
        :title="t.name"
        :label="`${t.departureDate?.slice(0, 10)} · 收/支比 ${t.incomeExpenseRatio}`"
        is-link
        @click="router.push({ name: 'tour-detail', params: { id: t.id } })"
      >
        <template #value>
          <span class="profit">净利 {{ money(t.netProfitEstimate) }}</span>
        </template>
      </van-cell>
    </van-cell-group>

    <van-grid :column-num="3" class="mt">
      <van-grid-item icon="orders-o" text="全部团队" @click="router.push({ name: 'tours' })" />
      <van-grid-item icon="balance-pay" text="报销审批" @click="router.push({ name: 'reimburse' })" />
      <van-grid-item icon="bell" text="尾款催收" @click="router.push({ name: 'collections' })" />
    </van-grid>

    <van-floating-bubble axis="xy" icon="plus" magnetic="x" @click="router.push({ name: 'tours' })" />
  </div>
  <van-loading v-else-if="loading" vertical class="loading">加载中…</van-loading>
</template>

<style scoped>
.page { padding-bottom: 80px; }
.link { color: #1989fa; font-size: 14px; }
.hero {
  margin: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #1989fa, #39a0ff);
  border-radius: 12px;
  color: #fff;
}
.hero-label { font-size: 13px; opacity: 0.9; }
.hero-num { font-size: 32px; font-weight: 700; margin-top: 8px; letter-spacing: -1px; }
.hero-sub { font-size: 13px; margin-top: 6px; opacity: 0.95; }
.mt { margin-top: 12px; }
.profit { color: #07c160; font-size: 13px; }
.loading { padding: 48px; }
</style>
