<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
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
    showToast(e.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page">
    <div class="brand">
      <h1>旅盈</h1>
      <p>旅行社轻量管理系统 · MVP</p>
    </div>
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field v-model="phone" name="phone" label="手机号" placeholder="手机号" :rules="[{ required: true }]" />
        <van-field v-model="password" type="password" name="password" label="密码" placeholder="密码" :rules="[{ required: true }]" />
      </van-cell-group>
      <div class="btn-wrap">
        <van-button round block type="primary" native-type="submit" :loading="loading">进入系统</van-button>
      </div>
    </van-form>
    <p class="hint">演示：老板 13800000001 / 业务员 13800000002，密码 demo123456</p>
  </div>
</template>

<style scoped>
.page { padding: 24px 0 48px; }
.brand { text-align: center; margin-bottom: 32px; }
.brand h1 { margin: 0; font-size: 28px; color: #1989fa; }
.brand p { margin: 8px 0 0; color: #969799; font-size: 14px; }
.btn-wrap { margin: 24px 16px; }
.hint { text-align: center; color: #969799; font-size: 12px; padding: 0 16px; }
</style>
