<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function approve(id) {
  try {
    await http.post(`/reimbursements/${id}/approve`)
    ElMessage.success('已通过')
    await load()
  } catch {
    ElMessage.error('失败')
  }
}

async function reject(id) {
  try {
    await http.post(`/reimbursements/${id}/partial-reject`, { note: '部分驳回，请补充票据' })
    ElMessage.success('已驳回')
    await load()
  } catch {
    ElMessage.error('失败')
  }
}
</script>

<template>
  <div v-if="auth.isBoss" v-loading="loading">
    <div class="toolbar">
      <span class="h">报销审批</span>
      <el-button type="primary" link @click="router.push({ name: 'batch-export' })">月结导出</el-button>
    </div>
    <el-collapse v-if="groups.length">
      <el-collapse-item v-for="g in groups" :key="`${g.tourId}-${g.staffUserId}`" :name="`${g.tourId}-${g.staffUserId}`">
        <template #title>
          <div class="collapse-title">
            <span>{{ g.tourCode }} · {{ g.staffName }}</span>
            <el-tag type="warning">¥{{ g.totalAdvance }}</el-tag>
          </div>
        </template>
        <div v-for="l in g.lines" :key="l.id" class="line">
          <span>{{ l.category }} {{ l.amount }} · {{ l.approvalStatus }}</span>
          <span>
            <el-button v-if="l.approvalStatus === 'PENDING'" size="small" type="primary" @click="approve(l.id)">通过</el-button>
            <el-button v-if="l.approvalStatus === 'PENDING'" size="small" @click="reject(l.id)">驳回</el-button>
          </span>
        </div>
      </el-collapse-item>
    </el-collapse>
    <el-empty v-else description="暂无待办报销" />
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
.collapse-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 12px;
}
.line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
  font-size: 13px;
}
</style>
