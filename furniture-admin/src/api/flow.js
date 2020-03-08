import request from '@/utils/request';

export function fetchList(data) {
  return request({
    url: '/wallet/list',
    method: 'post',
    data
  });
}
export function flowList(data) {
  return request({
    url: '/wallet/flow/list',
    method: 'post',
    data
  });
}
export function withdraw(data) {
  return request({
    url: '/wallet/withdraw',
    method: 'post',
    data
  });
}
export function topup(data) {
  return request({
    url: '/wallet/topup',
    method: 'post',
    data
  });
}
export function delPassword(data) {
  return request({
    url: '/wallet/password/delete',
    method: 'post',
    data
  });
}
