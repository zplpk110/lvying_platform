import request from '@/utils/request'

export function getDashboardOverview() {
  return request({
    url: '/lvying/dashboard/overview',
    method: 'get'
  })
}

export function openGroup(data) {
  return request({
    url: '/lvying/group/open',
    method: 'post',
    data: data
  })
}

export function getGroupDetail(groupId) {
  return request({
    url: '/lvying/group/' + groupId + '/detail',
    method: 'get'
  })
}

export function addGroupIncome(groupId, data) {
  return request({
    url: '/lvying/group/' + groupId + '/income',
    method: 'post',
    data: data
  })
}

export function addGroupCost(groupId, data) {
  return request({
    url: '/lvying/group/' + groupId + '/cost',
    method: 'post',
    data: data
  })
}

export function getOverdueList() {
  return request({
    url: '/lvying/collection/overdue',
    method: 'get'
  })
}

export function getMyWallet() {
  return request({
    url: '/lvying/wallet/me',
    method: 'get'
  })
}

export function getAdvanceSummary(settleMonth) {
  return request({
    url: '/lvying/advance/summary',
    method: 'get',
    params: { settleMonth }
  })
}

export function settleAdvance(data) {
  return request({
    url: '/lvying/advance/settle',
    method: 'post',
    data: data
  })
}

export function getAdvanceSettlementList(settleMonth) {
  return request({
    url: '/lvying/advance/settlement/list',
    method: 'get',
    params: { settleMonth }
  })
}
