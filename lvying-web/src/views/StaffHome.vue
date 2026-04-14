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
  } catch (e) {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

function money(s) {
  const n = Number(s)
  return `¥${n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function logout() {
  auth.clear()
  router.replace({ name: 'login' })
}
</script>

<template>
  <div class="page" v-if="data">
    <van-nav-bar title="我的任务与垫资" fixed placeholder>
      <template #right>
        <span class="link" @click="logout">退出</span>
      </template>
    </van-nav-bar>

    <div class="hero">
      <div class="hero-label">待公司还款</div>
      <div class="hero-num">{{ money(data.pendingRepaymentTotal) }}</div>
    </div>

    <van-cell-group inset title="近期垫付">
      <van-cell
        v-for="l in data.recentLines"
        :key="l.id"
        :title="`${l.date} · #${l.tourCode}`"
        :label="`${categoryLabel(l.category)} · ${l.displayStatus}`"
        :value="money(l.amount)"
      />
    </van-cell-group>

    <van-grid :column-num="2" class="mt">
      <van-grid-item icon="orders-o" text="团队列表" @click="router.push({ name: 'tours' })" />
      <van-grid-item icon="balance-pay" text="报销中心" @click="router.push({ name: 'reimburse' })" />
    </van-grid>

    <van-floating-bubble axis="xy" icon="photograph" magnetic="x" @click="router.push({ name: 'tours' })" />
  </div>
  <van-loading v-else-if="loading" vertical class="loading">加载中…</van-loading>
</template>

<style scoped>
.page { padding-bottom: 80px; }
.link { color: #1989fa; font-size: 14px; }
.hero {
  margin: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #07c160, #4cd964);
  border-radius: 12px;
  color: #fff;
}
.hero-label { font-size: 13px; opacity: 0.9; }
.hero-num { font-size: 28px; font-weight: 700; margin-top: 8px; }
.mt { margin-top: 12px; }
.loading { padding: 48px; }
</style>
