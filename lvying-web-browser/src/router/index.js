/**
 * 浏览器端：History 模式；侧栏路由与角色 meta。
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('../views/HomeRedirect.vue') },
      { path: 'boss', name: 'boss', component: () => import('../views/BossHome.vue'), meta: { roles: ['BOSS_FINANCE'] } },
      { path: 'staff', name: 'staff', component: () => import('../views/StaffHome.vue'), meta: { roles: ['SALES_GUIDE'] } },
      { path: 'tours', name: 'tours', component: () => import('../views/TourList.vue') },
      { path: 'tours/:id', name: 'tour-detail', component: () => import('../views/TourDetail.vue') },
      { path: 'reimburse', name: 'reimburse', component: () => import('../views/ReimburseCenter.vue'), meta: { roles: ['BOSS_FINANCE'] } },
      { path: 'collections', name: 'collections', component: () => import('../views/Collections.vue'), meta: { roles: ['BOSS_FINANCE'] } },
      { path: 'batch-export', name: 'batch-export', component: () => import('../views/BatchExport.vue'), meta: { roles: ['BOSS_FINANCE'] } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.public) return true
  if (!auth.token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.roles?.length && !to.meta.roles.includes(auth.user?.role)) {
    return { name: auth.isBoss ? 'boss' : 'staff' }
  }
  return true
})

export default router
