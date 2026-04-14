<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const phone = ref('13800000001')
const password = ref('demo123456')
const loading = ref(false)

async function onSubmit() {
  loading.value = true
  try {
    const { data } = await http.post('/auth/login', {
      phone: phone.value,
      password: password.value,
    })
    auth.setSession(data.accessToken, data.user)
    const redirect = route.query.redirect
    if (redirect) router.replace(String(redirect))
    else if (data.user.role === 'BOSS_FINANCE') router.replace({ name: 'boss' })
    else router.replace({ name: 'staff' })
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="wrap">
    <el-card class="card" shadow="hover">
      <div class="brand">
        <h1>旅盈</h1>
        <p>旅行社轻量管理系统 · 浏览器端</p>
      </div>
      <el-form label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="手机号">
          <el-input v-model="phone" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" show-password autocomplete="current-password" />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" class="btn">进入系统</el-button>
      </el-form>
      <p class="hint">演示：老板 13800000001 / 业务员 13800000002，密码 demo123456</p>
    </el-card>
  </div>
</template>

<style scoped>
.wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #e8f4ff 0%, #f5f7fa 50%, #fff 100%);
}
.card {
  width: 400px;
  max-width: 92vw;
}
.brand {
  text-align: center;
  margin-bottom: 24px;
}
.brand h1 {
  margin: 0;
  color: var(--el-color-primary);
  font-size: 28px;
}
.brand p {
  margin: 8px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}
.btn {
  width: 100%;
  margin-top: 8px;
}
.hint {
  margin: 16px 0 0;
  text-align: center;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
