<template>
  <div class="app-container">
    <el-row :gutter="16" class="mb12">
      <el-col :span="8">
        <el-card class="urgent-card" shadow="hover">
          <div slot="header">今日紧急待办</div>
          <div class="urgent-item" @click="$router.push('/lvying/collection')">
            尾款待催：<b>{{ data.unpaidCustomerCount || 0 }}人</b>
          </div>
          <div class="urgent-item" @click="gotoFirstWarningTour">
            成本预警团：<b>{{ warningTourCount }}个</b>
          </div>
          <div v-if="canApprove" class="urgent-item" @click="$router.push('/lvying/reimburse')">
            本月待还垫资：<b>¥{{ money(data.staffAdvanceAmount) }}</b>
          </div>
          <div v-if="canApprove" class="urgent-item" @click="$router.push('/lvying/reimburse')">
            累计待还垫资：<b>¥{{ money(data.staffAdvanceAmountTotal) }}</b>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="hover">
          <div slot="header">资金池概览</div>
          <el-row :gutter="10">
            <el-col :span="8">
              <div class="stat-box">
                <div class="label">实收总额</div>
                <div class="value money">¥{{ money(data.totalActualIncome) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-box">
                <div class="label">成本总额</div>
                <div class="value">¥{{ money(data.totalCost) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-box">
                <div class="label">预计利润</div>
                <div class="value">¥{{ money(data.estimatedProfit) }}</div>
              </div>
            </el-col>
          </el-row>
          <div v-if="canApprove" class="quick-entry">
            <el-button type="primary" size="mini" @click="$router.push('/lvying/reimburse')">去垫资结算</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <div slot="header">进行中团看板</div>
      <el-table :data="data.ongoingTours || []" v-loading="loading">
        <el-table-column label="团名" prop="groupName" />
        <el-table-column label="出团日期" width="130">
          <template slot-scope="scope">
            {{ parseTime(scope.row.departDate, '{y}-{m}-{d}') }}
          </template>
        </el-table-column>
        <el-table-column label="人数" prop="peopleCount" width="80" />
        <el-table-column label="预算成本" width="140">
          <template slot-scope="scope">
            ¥{{ money(scope.row.budgetCost) }}
          </template>
        </el-table-column>
        <el-table-column label="实时净利预估" width="160">
          <template slot-scope="scope">
            ¥{{ money(scope.row.estimatedProfit) }}
          </template>
        </el-table-column>
        <el-table-column label="预警" width="90">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.budgetWarnLevel === 2" type="danger">红灯</el-tag>
            <el-tag v-else-if="scope.row.budgetWarnLevel === 1" type="warning">黄灯</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button type="text" @click="goTour(scope.row.groupId)">团详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dropdown class="fab" trigger="click">
      <el-button type="primary" icon="el-icon-plus" circle size="medium"></el-button>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item @click.native="openGroupDialog = true">开新团</el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>

    <el-dialog title="开新团" :visible.sync="openGroupDialog" width="520px">
      <el-form label-width="100px">
        <el-form-item label="团名">
          <el-input v-model="groupForm.groupName" placeholder="例如：杭州2日团" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="出团日期">
              <el-date-picker v-model="groupForm.departDate" type="date" value-format="yyyy-MM-dd" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="返程日期">
              <el-date-picker v-model="groupForm.returnDate" type="date" value-format="yyyy-MM-dd" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="人数">
              <el-input-number v-model="groupForm.peopleCount" :min="1" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计价方式">
              <el-select v-model="groupForm.priceMode" style="width: 100%;">
                <el-option :value="1" label="人均" />
                <el-option :value="2" label="总包" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="groupForm.priceMode === 1" label="人均价">
          <el-input-number v-model="groupForm.unitPrice" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item v-else label="总包价">
          <el-input-number v-model="groupForm.totalPrice" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="预算成本">
          <el-input-number v-model="groupForm.budgetCost" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="openGroupDialog = false">取消</el-button>
        <el-button type="primary" @click="submitOpenGroup">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getDashboardOverview, openGroup } from '@/api/lvying/finance'
import { checkPermi } from '@/utils/permission'

export default {
  name: 'LvYingDashboard',
  data() {
    return {
      loading: false,
      data: {},
      canApprove: false,
      openGroupDialog: false,
      groupForm: {
        groupName: '',
        departDate: '',
        returnDate: '',
        peopleCount: 20,
        priceMode: 1,
        unitPrice: 0,
        totalPrice: 0,
        budgetCost: 0
      }
    }
  },
  computed: {
    warningTourCount() {
      return (this.data.ongoingTours || []).filter(item => Number(item.budgetWarnLevel) > 0).length
    }
  },
  created() {
    this.canApprove = checkPermi(['lvying:reimburse:approve'])
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      getDashboardOverview().then(res => {
        this.data = res.data || {}
      }).finally(() => {
        this.loading = false
      })
    },
    money(v) {
      return Number(v || 0).toFixed(2)
    },
    goTour(groupId) {
      this.$router.push('/lvying/tour/detail/' + groupId)
    },
    gotoFirstWarningTour() {
      const first = (this.data.ongoingTours || []).find(item => Number(item.budgetWarnLevel) > 0)
      if (!first) {
        this.$modal.msgWarning('暂无预警团')
        return
      }
      this.goTour(first.groupId)
    },
    submitOpenGroup() {
      openGroup(this.groupForm).then(res => {
        const id = res.data && res.data.id
        this.$modal.msgSuccess('开团成功')
        this.openGroupDialog = false
        this.loadData()
        if (id) {
          this.goTour(id)
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.mb12 { margin-bottom: 12px; }
.urgent-card { background: #fff5f5; }
.urgent-item { line-height: 32px; cursor: pointer; }
.stat-box { border: 1px solid #ebeef5; border-radius: 8px; padding: 12px; }
.label { color: #909399; margin-bottom: 6px; }
.value { font-size: 20px; font-weight: 700; }
.money { color: #f56c6c; }
.quick-entry { margin-top: 10px; text-align: right; }
.fab { position: fixed; right: 32px; bottom: 72px; }
</style>
