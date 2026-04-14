<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
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
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function remind(ids) {
  try {
    await ElMessageBox.confirm('向该团游客发送催收提醒（演示：写入系统日志）？', '催收', { type: 'warning' })
    const { data } = await http.post('/collections/bulk-remind', { tourIds: ids })
    ElMessage.success(`已记录 ${data.sent} 条`)
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('失败')
  }
}
</script>

<template>
  <div v-loading="loading">
    <h3 class="h">尾款催收</h3>
    <el-card v-for="t in list" :key="t.tourId" class="blk" shadow="never">
      <template #header>
        <div class="card-h">
          <span>{{ t.name }}（#{{ t.tourCode }}）</span>
          <el-tag type="danger">尾欠 {{ t.tailRemaining }} 元</el-tag>
        </div>
        <div class="sub">出团 {{ t.departureDate?.slice(0, 10) }} · 销售 {{ t.salesName || '—' }}</div>
      </template>
      <el-table :data="t.guests" size="small">
        <el-table-column prop="name" label="游客" />
        <el-table-column prop="phoneMasked" label="手机" />
        <el-table-column prop="balanceDue" label="欠费（元）" />
      </el-table>
      <el-button type="danger" class="mt" @click="remind([t.tourId])">一键群发提醒</el-button>
    </el-card>
    <el-empty v-if="!loading && !list.length" description="暂无尾款欠收" />
  </div>
</template>

<style scoped>
.h {
  margin: 0 0 16px;
  font-size: 16px;
}
.blk {
  margin-bottom: 16px;
}
.card-h {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.sub {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-top: 6px;
}
.mt {
  margin-top: 12px;
}
</style>
