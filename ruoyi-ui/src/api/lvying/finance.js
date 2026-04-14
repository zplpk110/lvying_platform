import request from '@/utils/request'

export function getDashboardOverview() {
  return request({
    url: '/lvying/dashboard/overview',
    method: 'get'
  })
}

export function getTourDetail(tourId) {
  return request({
    url: '/lvying/tour/' + tourId + '/detail',
    method: 'get'
  })
}

export function addTourIncome(tourId, data) {
  return request({
    url: '/lvying/tour/' + tourId + '/income',
    method: 'post',
    data: data
  })
}

export function addTourExpense(tourId, data, forceConfirm) {
  return request({
    url: '/lvying/tour/' + tourId + '/expense',
    method: 'post',
    params: { forceConfirm: !!forceConfirm },
    data: data
  })
}

export function getMyWallet() {
  return request({
    url: '/lvying/reimbursement/my-wallet',
    method: 'get'
  })
}

export function getReimbursementApprovals() {
  return request({
    url: '/lvying/reimbursement/approvals',
    method: 'get'
  })
}

export function approveReimbursement(costId, remark) {
  return request({
    url: '/lvying/reimbursement/' + costId + '/approve',
    method: 'post',
    data: { remark: remark }
  })
}

export function rejectReimbursement(costId, remark) {
  return request({
    url: '/lvying/reimbursement/' + costId + '/reject',
    method: 'post',
    data: { remark: remark }
  })
}

export function getOverdueList() {
  return request({
    url: '/lvying/collection/overdue',
    method: 'get'
  })
}

export function sendBatchReminder() {
  return request({
    url: '/lvying/collection/send-batch',
    method: 'post'
  })
}
