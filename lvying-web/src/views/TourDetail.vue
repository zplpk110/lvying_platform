<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
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
  } catch (_) {}
  await load()
})

async function load() {
  try {
    const { data } = await http.get(`/tours/${id.value}`)
    tour.value = data
  } catch (e) {
    showToast('加载失败')
  }
}

function money(s) {
  if (s == null) return '—'
  return `¥${Number(s).toLocaleString('zh-CN')}`
}

async function saveIncome() {
  if (!incomeForm.value.amount) {
    showToast('填写金额')
    return
  }
  try {
    await http.post(`/tours/${id.value}/incomes`, {
      amount: Number(incomeForm.value.amount),
      type: incomeForm.value.type,
      note: incomeForm.value.note || undefined,
    })
    showIncome.value = false
    showToast('已记录收款')
    await load()
  } catch (e) {
    showToast(e.response?.data?.message || '失败')
  }
}

async function saveExpense(forceBossConfirm) {
  if (!expenseForm.value.amount) {
    showToast('填写金额')
    return
  }
  if (expenseForm.value.paymentMethod === 'STAFF_ADVANCE' && !expenseForm.value.staffUserId) {
    showToast('选择垫付人')
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
    showToast('已记录支出')
    await load()
  } catch (e) {
    const code = e.response?.data?.code
    if (code === 'OVERSPEND_GUARD' && auth.isBoss) {
      try {
        await showConfirmDialog({ title: '超支确认', message: e.response?.data?.message })
        await saveExpense(true)
      } catch (_) {}
    } else {
      showToast(e.response?.data?.message || '失败')
    }
  }
}

async function doSettle() {
  try {
    await showConfirmDialog({ message: '确认申请封团结算？' })
    await http.post(`/tours/${id.value}/settle`)
    showToast('已封团')
    await load()
  } catch (e) {
    if (e !== 'cancel') showToast('操作失败或无权')
  }
}
</script>

<template>
  <div v-if="tour" class="page">
    <van-nav-bar :title="tour.name" left-arrow @click-left="router.back()" fixed placeholder />
    <div class="head">
      <span class="tag">#{{ tour.tourCode }}</span>
      <span class="st">{{ tour.status }}</span>
    </div>

    <div class="card green">
      <h3>收银台</h3>
      <p>总应收 {{ money(tour.finance?.totalReceivable) }}（{{ tour.guestCount }}人 × {{ money(tour.pricePerGuest) }}）</p>
      <p>已收定金 {{ money(tour.finance?.depositReceived) }}</p>
      <p>已收尾款 {{ money(tour.finance?.balanceReceived) }}</p>
      <p class="emph">尚欠尾款 {{ money(tour.finance?.tailOwed) }}</p>
      <van-button type="success" size="small" block round @click="showIncome = true">记一笔收款</van-button>
    </div>

    <div class="card red">
      <h3>成本夹</h3>
      <p>预算红线 {{ money(tour.finance?.budgetRedline) }}（毛利率 {{ tour.grossMarginPct }}%）</p>
      <p>当前已花费 {{ money(tour.finance?.paidCost) }}</p>
      <p>待支付/待报销 {{ money(tour.finance?.pendingStaffCost) }}</p>
      <p>总成本预估 {{ money(tour.finance?.estimatedTotalCost) }}</p>
      <p class="emph good">实时净利 {{ money(tour.finance?.netProfitEstimate) }}（较预算盈余 {{ money(tour.finance?.budgetSurplus) }}）</p>
      <van-button type="danger" size="small" block round plain @click="showExpense = true">记一笔支出/报销</van-button>
    </div>

    <div class="card gold">
      <h3>利润结算</h3>
      <p>提成比例净利润的 {{ tour.commissionRate }}%</p>
      <p class="emph">预估提成 {{ money(tour.finance?.commissionEstimate) }}</p>
      <p class="formula">净利公式：已收款 {{ money(tour.finance?.incomeReceived) }} − 总成本预估 {{ money(tour.finance?.estimatedTotalCost) }} = {{ money(tour.finance?.netProfitEstimate) }}</p>
      <van-button v-if="auth.isBoss" type="primary" size="small" block round @click="doSettle">申请封团结算</van-button>
    </div>
  </div>

  <van-popup v-model:show="showIncome" position="bottom" round>
    <div class="form-pop">
      <h3>记一笔收款</h3>
      <van-field v-model="incomeForm.amount" type="number" label="金额" placeholder="元" />
      <van-field label="类型">
        <template #input>
          <van-radio-group v-model="incomeForm.type" direction="horizontal">
            <van-radio name="DEPOSIT">定金</van-radio>
            <van-radio name="BALANCE">尾款</van-radio>
            <van-radio name="OTHER">其他</van-radio>
          </van-radio-group>
        </template>
      </van-field>
      <van-field v-model="incomeForm.note" label="备注" />
      <div class="row-btns">
        <van-button block @click="showIncome = false">取消</van-button>
        <van-button block type="primary" @click="saveIncome">保存</van-button>
      </div>
    </div>
  </van-popup>

  <van-popup v-model:show="showExpense" position="bottom" round>
    <div class="form-pop">
      <h3>记支出 / 报销</h3>
      <van-field v-model="expenseForm.amount" type="number" label="金额" placeholder="元" />
      <van-field label="类别">
        <template #input>
          <van-radio-group v-model="expenseForm.category" direction="horizontal">
            <van-radio name="LODGING">住宿</van-radio>
            <van-radio name="DINING">餐饮</van-radio>
            <van-radio name="TICKET">门票</van-radio>
            <van-radio name="TRANSPORT">交通</van-radio>
            <van-radio name="DRIVER_GUIDE">司导</van-radio>
          </van-radio-group>
        </template>
      </van-field>
      <van-field label="支付方式">
        <template #input>
          <van-radio-group v-model="expenseForm.paymentMethod" direction="horizontal">
            <van-radio name="COMPANY_ACCOUNT">公账支付</van-radio>
            <van-radio name="STAFF_ADVANCE">员工垫付</van-radio>
          </van-radio-group>
        </template>
      </van-field>
      <van-field v-if="expenseForm.paymentMethod === 'STAFF_ADVANCE'" label="垫付人">
        <template #input>
          <select v-model="expenseForm.staffUserId" class="sel">
            <option disabled value="">下拉选择姓名</option>
            <option v-for="s in staffOptions" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </template>
      </van-field>
      <div class="row-btns">
        <van-button block @click="showExpense = false">取消</van-button>
        <van-button block type="primary" @click="saveExpense(false)">提交</van-button>
      </div>
    </div>
  </van-popup>
</template>

<style scoped>
.page { padding: 8px 12px 80px; }
.head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.tag { font-weight: 600; color: #323233; }
.st { font-size: 12px; color: #969799; }
.card { border-radius: 10px; padding: 12px; margin-bottom: 12px; }
.card h3 { margin: 0 0 8px; font-size: 16px; }
.card p { margin: 4px 0; font-size: 13px; color: #646566; }
.card.green { background: #e8f7ef; border: 1px solid #07c16033; }
.card.red { background: #fff0f0; border: 1px solid #ee0a2433; }
.card.gold { background: #fff8e6; border: 1px solid #ff976a33; }
.emph { font-weight: 600; color: #323233 !important; }
.good { color: #07c160 !important; }
.formula { font-size: 12px !important; line-height: 1.4; }
.form-pop { padding: 16px; padding-bottom: 24px; }
.form-pop h3 { margin: 0 0 12px; text-align: center; }
.row-btns { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-top: 12px; }
.sel { width: 100%; padding: 8px; border-radius: 6px; border: 1px solid #dcdee0; }
</style>
