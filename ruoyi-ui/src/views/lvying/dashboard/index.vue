<template>
  <div class="app-container">
    <el-row :gutter="16" class="mb12">
      <el-col :span="8">
        <el-card class="urgent-card">
          <div slot="header">今日紧急待办</div>
          <div class="urgent-item" @click="$router.push('/lvying/reimburse')">
            需审批报销：<b>{{ data.pendingApprovalCount || 0 }}笔</b>
          </div>
          <div class="urgent-item" @click="$router.push('/lvying/collection')">
            尾款逾期未收团：<b>{{ data.overdueFinalCount || 0 }}个</b>
          </div>
          <div class="urgent-item" @click="gotoFirstWarningTour">
            毛利预警团：<b>{{ data.grossWarningCount || 0 }}个</b>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card>
          <div slot="header">资金池概览</div>
          <el-row :gutter="10">
            <el-col :span="8">
              <div class="stat-box">
                <div class="label">可用余额</div>
                <div class="value money">¥{{ money(data.availableBalance) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-box">
                <div class="label">在途垫款（欠员工）</div>
                <div class="value">¥{{ money(data.totalPendingCost) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-box">
                <div class="label">预计本月净利润</div>
                <div class="value">¥{{ money(data.estimatedMonthlyProfit) }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <div slot="header">进行中团看板</div>
      <el-table :data="data.ongoingTours || []" v-loading="loading">
        <el-table-column label="团名" prop="tourName" />
        <el-table-column label="出团日期" width="130">
          <template slot-scope="scope">
            {{ parseTime(scope.row.departDate, '{y}-{m}-{d}') }}
          </template>
        </el-table-column>
        <el-table-column label="当前收/支比" prop="incomeCostRatio" width="140" />
        <el-table-column label="实时净利预估" width="160">
          <template slot-scope="scope">
            ¥{{ money(scope.row.estimatedProfit) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button type="text" @click="goTour(scope.row.tourId)">团详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dropdown class="fab" trigger="click">
      <el-button type="primary" icon="el-icon-plus" circle size="medium"></el-button>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item @click.native="goTour(1)">新建团期（MVP演示跳转）</el-dropdown-item>
        <el-dropdown-item @click.native="$modal.msgSuccess('扫码核销将在二期实现')">扫码核销</el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</template>

<script>
import { getDashboardOverview } from '@/api/lvying/finance'

export default {
  name: 'LvYingDashboard',
  data() {
    return {
      loading: false,
      data: {}
    }
  },
  created() {
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
    goTour(tourId) {
      this.$router.push('/lvying/tour/detail/' + tourId)
    },
    gotoFirstWarningTour() {
      const first = (this.data.ongoingTours || [])[0]
      if (!first) {
        this.$modal.msgWarning('暂无可查看团期')
        return
      }
      this.goTour(first.tourId)
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
.fab { position: fixed; right: 32px; bottom: 72px; }
</style>
