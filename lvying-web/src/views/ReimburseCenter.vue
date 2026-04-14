<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const groups = ref([])
const loading = ref(true)

onMounted(load)

async function load() {
  loading.value = true
  try {
    if (auth.isBoss) {
      const { data } = await http.get('/reimbursements/pending-by-tour')
      groups.value = data
    } else {
      router.replace({ name: 'staff' })
    }
  } catch (e) {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

async function approve(id) {
  try {
    await http.post(`/reimbursements/${id}/approve`)
    showToast('已通过')
    await load()
  } catch (e) {
    showToast('失败')
  }
}

async function reject(id) {
  try {
    await http.post(`/reimbursements/${id}/partial-reject`, { note: '部分驳回，请补充票据' })
    showToast('已驳回')
    await load()
  } catch (e) {
    showToast('失败')
  }
}
</script>

<template>
  <div class="page" v-if="auth.isBoss">
    <van-nav-bar title="报销审批" left-arrow @click-left="router.back()" fixed placeholder>
      <template #right>
        <span class="link" @click="router.push({ name: 'batch-export' })">月结导出</span>
      </template>
    </van-nav-bar>
    <van-loading v-if="loading" vertical class="ld">加载中…</van-loading>
    <van-collapse v-else accordion>
      <van-collapse-item
        v-for="g in groups"
        :key="`${g.tourId}-${g.staffUserId}`"
        :title="`${g.tourCode} · ${g.staffName}`"
        :value="`¥${g.totalAdvance}`"
      >
        <p class="sub">垫付明细：</p>
        <p v-for="l in g.lines" :key="l.id" class="line">
          {{ l.category }} {{ l.amount }} · {{ l.approvalStatus }}
          <van-button v-if="l.approvalStatus === 'PENDING'" size="mini" type="primary" @click="approve(l.id)">通过</van-button>
          <van-button v-if="l.approvalStatus === 'PENDING'" size="mini" @click="reject(l.id)">驳回</van-button>
        </p>
      </van-collapse-item>
    </van-collapse>
    <van-empty v-if="!loading && !groups.length" description="暂无待办报销" />
  </div>
</template>

<style scoped>
.page { min-height: 100vh; }
.link { color: #1989fa; font-size: 13px; }
.ld { padding: 48px; }
.sub { font-size: 13px; color: #646566; margin: 0 0 8px; }
.line { font-size: 13px; margin: 6px 0; display: flex; flex-wrap: wrap; gap: 6px; align-items: center; }
</style>
