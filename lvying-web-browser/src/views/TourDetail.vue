<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const tour = ref(null)
const staffOptions = ref([])
const showIncome = ref(false)
const showExpense = ref(false)
const incomeForm = ref({ amount: '', type: 'DEPOSIT', note: '' })
const expenseForm = ref({
  amount: '',
  category: 'TRANSPORT',
  paymentMethod: 'COMPANY_ACCOUNT',
  staffUserId: '',
})

const id = computed(() => route.params.id)

onMounted(async () => {
  try {
    const { data } = await http.get('/users/staff-options')
    staffOptions.value = data
  } catch {
    /* ignore */
  }
  await load()
})

async function load() {
  try {
    const { data } = await http.get(`/tours/${id.value}`)
    tour.value = data
  } catch {
    ElMessage.error('加载失败')
  }
}

function money(s) {
  if (s == null) return '—'
  return `¥${Number(s).toLocaleString('zh-CN')}`
}

async function saveIncome() {
  if (!incomeForm.value.amount) {
    ElMessage.warning('填写金额')
    return
  }
  try {
    await http.post(`/tours/${id.value}/incomes`, {
      amount: Number(incomeForm.value.amount),
      type: incomeForm.value.type,
      note: incomeForm.value.note || undefined,
    })
    showIncome.value = false
    ElMessage.success('已记录收款')
    await load()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '失败')
  }
}

async function saveExpense(forceBossConfirm) {
  if (!expenseForm.value.amount) {
    ElMessage.warning('填写金额')
    return
  }
  if (expenseForm.value.paymentMethod === 'STAFF_ADVANCE' && !expenseForm.value.staffUserId) {
    ElMessage.warning('选择垫付人')
    return
  }
  try {
    await http.post(`/tours/${id.value}/expenses`, {
      amount: Number(expenseForm.value.amount),
      category: expenseForm.value.category,
      paymentMethod: expenseForm.value.paymentMethod,
      staffUserId:
        expenseForm.value.paymentMethod === 'STAFF_ADVANCE'
          ? expenseForm.value.staffUserId
          : undefined,
      bossConfirmed: forceBossConfirm ? true : undefined,
    })
    showExpense.value = false
    ElMessage.success('已记录支出')
    await load()
  } catch (e) {
    const code = e.response?.data?.code
    if (code === 'OVERSPEND_GUARD' && auth.isBoss) {
      try {
        await ElMessageBox.confirm(e.response?.data?.message || '超支确认', '超支确认', {
          type: 'warning',
        })
        await saveExpense(true)
      } catch {
        /* cancel */
      }
    } else {
      ElMessage.error(e.response?.data?.message || '失败')
    }
  }
}

async function doSettle() {
  try {
    await ElMessageBox.confirm('确认申请封团结算？', '封团', { type: 'warning' })
    await http.post(`/tours/${id.value}/settle`)
    ElMessage.success('已封团')
    await load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败或无权')
  }
}
</script>

<template>
  <div v-if="tour">
    <div class="head">
      <el-button link type="primary" @click="router.push({ name: 'tours' })">← 返回列表</el-button>
      <div class="title-row">
        <h2>{{ tour.name }}</h2>
        <el-tag>{{ tour.tourCode }}</el-tag>
        <el-tag type="info">{{ tour.status }}</el-tag>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="never" class="card green">
          <template #header>收银台</template>
          <p>总应收 {{ money(tour.finance?.totalReceivable) }}（{{ tour.guestCount }} 人 × {{ money(tour.pricePerGuest) }}）</p>
          <p>已收定金 {{ money(tour.finance?.depositReceived) }}</p>
          <p>已收尾款 {{ money(tour.finance?.balanceReceived) }}</p>
          <p class="emph">尚欠尾款 {{ money(tour.finance?.tailOwed) }}</p>
          <el-button type="success" @click="showIncome = true">记一笔收款</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="card red">
          <template #header>成本夹</template>
          <p>预算红线 {{ money(tour.finance?.budgetRedline) }}（毛利率 {{ tour.grossMarginPct }}%）</p>
          <p>当前已花费 {{ money(tour.finance?.paidCost) }}</p>
          <p>待支付/待报销 {{ money(tour.finance?.pendingStaffCost) }}</p>
          <p>总成本预估 {{ money(tour.finance?.estimatedTotalCost) }}</p>
          <p class="emph good">实时净利 {{ money(tour.finance?.netProfitEstimate) }}</p>
          <el-button type="danger" plain @click="showExpense = true">记支出 / 报销</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="card gold">
          <template #header>利润结算</template>
          <p>提成比例 {{ tour.commissionRate }}%</p>
          <p class="emph">预估提成 {{ money(tour.finance?.commissionEstimate) }}</p>
          <p class="small">已收款 {{ money(tour.finance?.incomeReceived) }} − 总成本预估 {{ money(tour.finance?.estimatedTotalCost) }}</p>
          <el-button v-if="auth.isBoss" type="primary" @click="doSettle">申请封团结算</el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showIncome" title="记一笔收款" width="480px">
      <el-form label-width="80px">
        <el-form-item label="金额">
          <el-input v-model="incomeForm.amount" type="number" placeholder="元" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="incomeForm.type">
            <el-radio value="DEPOSIT">定金</el-radio>
            <el-radio value="BALANCE">尾款</el-radio>
            <el-radio value="OTHER">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="incomeForm.note" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showIncome = false">取消</el-button>
        <el-button type="primary" @click="saveIncome">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showExpense" title="记支出 / 报销" width="520px">
      <el-form label-width="100px">
        <el-form-item label="金额">
          <el-input v-model="expenseForm.amount" type="number" placeholder="元" />
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="expenseForm.category" style="width: 100%">
            <el-option label="住宿" value="LODGING" />
            <el-option label="餐饮" value="DINING" />
            <el-option label="门票" value="TICKET" />
            <el-option label="交通" value="TRANSPORT" />
            <el-option label="司导" value="DRIVER_GUIDE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="expenseForm.paymentMethod">
            <el-radio value="COMPANY_ACCOUNT">公账支付</el-radio>
            <el-radio value="STAFF_ADVANCE">员工垫付</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="expenseForm.paymentMethod === 'STAFF_ADVANCE'" label="垫付人">
          <el-select v-model="expenseForm.staffUserId" placeholder="选择" style="width: 100%">
            <el-option v-for="s in staffOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showExpense = false">取消</el-button>
        <el-button type="primary" @click="saveExpense(false)">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.head {
  margin-bottom: 16px;
}
.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}
.title-row h2 {
  margin: 0;
  font-size: 20px;
}
.card p {
  margin: 6px 0;
  font-size: 13px;
  color: var(--el-text-color-regular);
}
.card.green {
  border: 1px solid var(--el-color-success-light-5);
}
.card.red {
  border: 1px solid var(--el-color-danger-light-5);
}
.card.gold {
  border: 1px solid var(--el-color-warning-light-5);
}
.emph {
  font-weight: 600;
  color: var(--el-text-color-primary) !important;
}
.good {
  color: var(--el-color-success) !important;
}
.small {
  font-size: 12px !important;
  line-height: 1.5;
}
</style>
