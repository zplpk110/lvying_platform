<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const list = ref([])
const loading = ref(true)

onMounted(async () => {
  if (!auth.isBoss) {
    router.replace({ name: 'staff' })
    return
  }
  await load()
})

async function load() {
  loading.value = true
  try {
    const { data } = await http.get('/collections/overdue-balance')
    list.value = data
  } catch (e) {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

async function remind(ids) {
  try {
    await showConfirmDialog({ message: '向该团游客发送催收提醒（演示：写入系统日志）？' })
    const { data } = await http.post('/collections/bulk-remind', { tourIds: ids })
    showToast(`已记录 ${data.sent} 条`)
  } catch (e) {
    if (e !== 'cancel') showToast('失败')
  }
}

function tailLabel(t) {
  return `尾欠 ${t.tailRemaining} 元`
}
</script>

<template>
  <div class="page">
    <van-nav-bar title="尾款催收" left-arrow @click-left="router.back()" fixed placeholder />
    <van-loading v-if="loading" vertical class="ld">加载中…</van-loading>
    <template v-else>
      <van-cell-group inset v-for="t in list" :key="t.tourId" class="blk">
        <van-cell :title="t.name" :label="`#${t.tourCode} · 出团 ${t.departureDate?.slice(0, 10)}`" :value="tailLabel(t)" />
        <van-cell v-for="g in t.guests" :key="g.name + g.phoneMasked" :title="g.name" :label="g.phoneMasked" :value="`${g.balanceDue} 元`" />
        <van-cell>
          <van-button type="danger" size="small" block round @click="remind([t.tourId])">一键群发提醒</van-button>
        </van-cell>
      </van-cell-group>
      <van-empty v-if="!list.length" description="暂无尾款欠收" />
    </template>
  </div>
</template>

<style scoped>
.page { padding-bottom: 24px; }
.ld { padding: 48px; }
.blk { margin-bottom: 12px; }
</style>
