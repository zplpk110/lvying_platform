<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const list = ref([])
const staffOptions = ref([])
const showCreate = ref(false)
const pickSalesVisible = ref(false)
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
    } catch {
      /* ignore */
    }
  }
})

async function load() {
  try {
    const { data } = await http.get('/tours')
    list.value = data
  } catch {
    ElMessage.error('加载失败')
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

function selectSales(row) {
  form.value.salesUserId = row.id
  pickSalesVisible.value = false
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
    ElMessage.success('已创建')
    await load()
    router.push({ name: 'tour-detail', params: { id: data.id } })
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '创建失败')
  } finally {
    creating.value = false
  }
}
</script>

<template>
  <div>
    <div class="toolbar">
      <span class="h">进行中团队</span>
      <el-button v-if="auth.isBoss" type="primary" @click="openCreate">新建团期</el-button>
    </div>

    <el-table :data="list" stripe @row-click="(row) => router.push({ name: 'tour-detail', params: { id: row.id } })">
      <el-table-column prop="tourCode" label="团号" width="110" />
      <el-table-column prop="name" label="行程" min-width="180" />
      <el-table-column prop="departureDate" label="出团日" width="120" />
      <el-table-column prop="salesName" label="销售" width="100">
        <template #default="{ row }">{{ row.salesName || '—' }}</template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showCreate" title="新建团期" width="520px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="团号">
          <el-input v-model="form.tourCode" placeholder="如 HZ0420" />
        </el-form-item>
        <el-form-item label="行程名">
          <el-input v-model="form.name" placeholder="如 杭州2日游" />
        </el-form-item>
        <el-form-item label="出团日">
          <el-input v-model="form.departureDate" placeholder="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="人数">
          <el-input-number v-model="form.guestCount" :min="0" />
        </el-form-item>
        <el-form-item label="单价">
          <el-input-number v-model="form.pricePerGuest" :min="0" />
        </el-form-item>
        <el-form-item v-if="staffOptions.length" label="销售">
          <el-button @click="pickSalesVisible = true">{{ salesName() }}</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="pickSalesVisible" title="选择销售" width="400px">
      <el-table :data="staffOptions" @row-click="selectSales">
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="phone" label="手机" />
      </el-table>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.h {
  font-size: 16px;
  font-weight: 600;
}
</style>
