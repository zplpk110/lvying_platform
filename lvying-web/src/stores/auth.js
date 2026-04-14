import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const TOKEN_KEY = 'lvying_token'
const USER_KEY = 'lvying_user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  const isBoss = computed(() => user.value?.role === 'BOSS_FINANCE')
  const isStaff = computed(() => user.value?.role === 'SALES_GUIDE')

  function setSession(t, u) {
    token.value = t
    user.value = u
    localStorage.setItem(TOKEN_KEY, t)
    localStorage.setItem(USER_KEY, JSON.stringify(u))
  }

  function clear() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, user, isBoss, isStaff, setSession, clear }
})
