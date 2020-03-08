import request from '@/utils/request';

export function fetchList(data) {
  return request({
    url: '/user/list',
    method: 'post',
    data
  });
}
export function allList(data) {
  return request({
    url: '/user/all/list',
    method: 'post',
    data
  });
}

export function addUser(data) {
  return request({
    url: '/user/add',
    method: 'post',
    data
  });
}
export function updateUser(data) {
  return request({
    url: '/user/update',
    method: 'post',
    data
  });
}
export function changeUser(data) {
  return request({
    url: '/user/update/status',
    method: 'post',
    data
  });
}

export function listAddress(query) {
  return request({
    url: '/address/list',
    method: 'get',
    params: query
  });
}

export function listCollect(query) {
  return request({
    url: '/collect/list',
    method: 'get',
    params: query
  });
}

export function listFeedback(query) {
  return request({
    url: '/feedback/list',
    method: 'get',
    params: query
  });
}

export function listFootprint(query) {
  return request({
    url: '/footprint/list',
    method: 'get',
    params: query
  });
}

export function listHistory(query) {
  return request({
    url: '/history/list',
    method: 'get',
    params: query
  });
}
export function userUpdate(data) {
  return request({
    url: '/user/user/admin/update',
    method: 'post',
    data
  });
}
