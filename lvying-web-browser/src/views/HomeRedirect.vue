<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

onMounted(() => {
  if (!auth.token) {
    router.replace({ name: 'login' })
    return
  }
  if (auth.isBoss) router.replace({ name: 'boss' })
  else router.replace({ name: 'staff' })
})
</script>

<template>
  <div class="center">
    <el-icon class="spin"><Loading /></el-icon>
    <span>加载中…</span>
  </div>
</template>

<style scoped>
.center {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 48px;
  color: var(--el-text-color-secondary);
}
.spin {
  font-size: 22px;
  animation: rot 0.9s linear infinite;
}
@keyframes rot {
  to {
    transform: rotate(360deg);
  }
}
</style>
