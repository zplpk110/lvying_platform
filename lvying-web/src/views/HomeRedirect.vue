<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
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
  <van-loading vertical>加载中…</van-loading>
</template>
