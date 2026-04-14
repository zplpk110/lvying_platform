<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const activeMenu = computed(() => {
  const p = route.path
  if (p.startsWith('/tours/')) return '/tours'
  return p || '/'
})

function logout() {
  auth.clear()
  router.replace({ name: 'login' })
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">旅盈 · 桌面端</div>
      <el-menu :default-active="activeMenu" router class="menu">
        <el-menu-item v-if="auth.isBoss" index="/boss">
          <span>经营仪表盘</span>
        </el-menu-item>
        <el-menu-item v-if="auth.isStaff" index="/staff">
          <span>我的工作台</span>
        </el-menu-item>
        <el-menu-item index="/tours">
          <span>进行中团队</span>
        </el-menu-item>
        <el-menu-item v-if="auth.isBoss" index="/reimburse">
          <span>报销审批</span>
        </el-menu-item>
        <el-menu-item v-if="auth.isBoss" index="/collections">
          <span>尾款催收</span>
        </el-menu-item>
        <el-menu-item v-if="auth.isBoss" index="/batch-export">
          <span>月结代发导出</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="title">{{ route.meta.title || '旅盈' }}</span>
        <div class="right">
          <span class="user">{{ auth.user?.name }}（{{ auth.user?.phone }}）</span>
          <el-button type="primary" link @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  min-height: 100vh;
}
.aside {
  background: #1d1e1f;
  color: #fff;
}
.logo {
  padding: 20px 16px;
  font-weight: 600;
  font-size: 15px;
  border-bottom: 1px solid #333;
}
.menu {
  border-right: none;
  background: transparent;
}
.menu :deep(.el-menu-item) {
  color: #c9cdd4;
}
.menu :deep(.el-menu-item.is-active) {
  background: #409eff33 !important;
  color: #fff;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--el-border-color);
  background: #fff;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}
.main {
  background: var(--el-bg-color-page);
  min-height: calc(100vh - 60px);
}
</style>
