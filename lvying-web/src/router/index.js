/**
 * 路由：Hash 模式便于静态部署；除 login 外需 token（守卫内检查）。
 */
import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue'), meta: { public: true } },
  { path: '/', name: 'home', component: () => import('../views/HomeRedirect.vue') },
  { path: '/boss', name: 'boss', component: () => import('../views/BossHome.vue') },
  { path: '/staff', name: 'staff', component: () => import('../views/StaffHome.vue') },
  { path: '/tours', name: 'tours', component: () => import('../views/TourList.vue') },
  { path: '/tours/:id', name: 'tour-detail', component: () => import('../views/TourDetail.vue') },
  { path: '/reimburse', name: 'reimburse', component: () => import('../views/ReimburseCenter.vue') },
  { path: '/collections', name: 'collections', component: () => import('../views/Collections.vue') },
  { path: '/batch-export', name: 'batch-export', component: () => import('../views/BatchExport.vue') },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.public) return true
  if (!auth.token) return { name: 'login', query: { redirect: to.fullPath } }
  if (to.name === 'home') return true
  return true
})

export default router
