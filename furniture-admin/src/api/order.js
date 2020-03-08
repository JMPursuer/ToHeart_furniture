import request from '@/utils/request';

export function listOrder(data) {
  return request({
    url: '/order/list',
    method: 'post',
    data
  });
}
export function AllListOrder(data) {
  return request({
    url: '/order/all/list',
    method: 'post',
    data
  });
}

export function detailOrder(id) {
  return request({
    url: '/order/detail',
    method: 'get',
    params: { id }
  });
}

export function shipOrder(data) {
  return request({
    url: '/order/ship',
    method: 'post',
    data
  });
}

export function refundOrder(data) {
  return request({
    url: '/order/refund',
    method: 'post',
    data
  });
}

export function replyComment(data) {
  return request({
    url: '/order/reply',
    method: 'post',
    data
  });
}

export function listChannel(id) {
  return request({
    url: '/order/channel',
    method: 'get'
  });
}

export function finalPayApi(data) {
  return request({
    url: '/order/finalpay',
    method: 'post',
    data
  });
}
export function finalpayUpdate(data) {
  return request({
    url: '/order/owe/finalpay/update',
    method: 'post',
    data
  });
}
export function deliveryUpdate(data) {
  return request({
    url: '/order/delivery/update',
    method: 'post',
    data
  });
}
