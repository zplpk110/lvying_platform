<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const list = ref([])
const staffOptions = ref([])
const showCreate = ref(false)
const pickSales = ref(false)
const creating = ref(false)
const form = ref({
  tourCode: '',
  name: '',
  departureDate: '',
  guestCount: 20,
  pricePerGuest: 1000,
  salesUserId: '',
})

onMounted(async () => {
  await load()
  if (auth.isBoss) {
    try {
      const { data } = await http.get('/users/staff-options')
      staffOptions.value = data
    } catch (_) {}
  }
})

async function load() {
  try {
    const { data } = await http.get('/tours')
    list.value = data
  } catch (e) {
    showToast('加载失败')
  }
}

function openCreate() {
  if (!auth.isBoss) return
  form.value = {
    tourCode: '',
    name: '',
    departureDate: new Date().toISOString().slice(0, 10),
    guestCount: 20,
    pricePerGuest: 1000,
    salesUserId: staffOptions.value[0]?.id || '',
  }
  showCreate.value = true
}

function salesName() {
  return staffOptions.value.find((s) => s.id === form.value.salesUserId)?.name || '选择销售'
}

async function submitCreate() {
  creating.value = true
  try {
    const body = {
      tourCode: form.value.tourCode,
      name: form.value.name,
      departureDate: form.value.departureDate,
      guestCount: Number(form.value.guestCount),
      pricePerGuest: Number(form.value.pricePerGuest),
      salesUserId: form.value.salesUserId || undefined,
    }
    const { data } = await http.post('/tours', body)
    showCreate.value = false
    showToast('已创建')
    await load()
    router.push({ name: 'tour-detail', params: { id: data.id } })
  } catch (e) {
    showToast(e.response?.data?.message || '创建失败')
  } finally {
    creating.value = false
  }
}
</script>

<template>
  <van-nav-bar title="进行中团队" left-arrow @click-left="router.back()" fixed placeholder>
    <template #right>
      <van-icon v-if="auth.isBoss" name="plus" size="20" @click="openCreate" />
    </template>
  </van-nav-bar>
  <van-cell-group inset>
    <van-cell
      v-for="t in list"
      :key="t.id"
      :title="t.name"
      :label="`#${t.tourCode} · ${t.departureDate} · ${t.salesName || '未指定销售'}`"
      is-link
      @click="router.push({ name: 'tour-detail', params: { id: t.id } })"
    />
  </van-cell-group>

  <van-popup v-model:show="showCreate" position="bottom" round :style="{ height: '72%' }">
    <div class="pop">
      <h3>新建团期</h3>
      <van-field v-model="form.tourCode" label="团号" placeholder="如 NJ0501" />
      <van-field v-model="form.name" label="行程名" placeholder="如 南京2日游" />
      <van-field v-model="form.departureDate" label="出团日" placeholder="YYYY-MM-DD" />
      <van-field v-model="form.guestCount" label="人数" type="digit" />
      <van-field v-model="form.pricePerGuest" label="单价" type="number" />
      <van-field
        v-if="staffOptions.length"
        :model-value="salesName()"
        label="销售"
        readonly
        is-link
        @click="pickSales = true"
      />
      <div class="pop-btn">
        <van-button block type="primary" round :loading="creating" @click="submitCreate">保存</van-button>
      </div>
    </div>
  </van-popup>

  <van-action-sheet
    v-model:show="pickSales"
    :actions="staffOptions.map((s) => ({ name: s.name, id: s.id }))"
    @select="(a) => { form.salesUserId = a.id; pickSales = false }"
  />
</template>

<style scoped>
.pop { padding: 16px; }
.pop h3 { margin: 0 0 12px; text-align: center; }
.pop-btn { margin-top: 16px; }
</style>
