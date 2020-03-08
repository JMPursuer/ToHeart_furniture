import request from '@/utils/request';

export function statUser(query) {
  return request({
    url: '/stat/user',
    method: 'get',
    params: query
  });
}

export function statOrder(query) {
  return request({
    url: '/stat/order',
    method: 'get',
    params: query
  });
}

export function statGoods(query) {
  return request({
    url: '/stat/goods',
    method: 'get',
    params: query
  });
}
/**
 * 获取客户的消费金额统计
 * @param {Object} data
 */
export function buyerList(data) {
  return request({
    url: '/performance/buyer/list',
    method: 'post',
    data
  });
}
/**
 * 获取所有销售员的业绩统计
 * @param {Object} data
 */
export function sellerList(data) {
  return request({
    url: '/performance/seller/list',
    method: 'post',
    data
  });
}
/**
 * 获取销售员自己的月度业绩统计
 * @param {Object} data
 */
export function sellerSelfList(data) {
  return request({
    url: '/performance/seller/self/month/list',
    method: 'post',
    data
  });
}
/**
 * 获取所有销售员的月度业绩统计
 * @param {Object} data
 */
export function sellerMonthList(data) {
  return request({
    url: '/performance/seller/month/list',
    method: 'post',
    data
  });
}
