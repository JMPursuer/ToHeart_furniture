import request from '@/utils/request';

export function listGoods(query) {
  return request({
    url: '/goods/list',
    method: 'get',
    params: query
  });
}

export function deleteGoods(data) {
  return request({
    url: '/goods/delete',
    method: 'post',
    data
  });
}

export function publishGoods(data) {
  return request({
    url: '/goods/create',
    method: 'post',
    data
  });
}

export function detailGoods(id) {
  return request({
    url: '/goods/detail',
    method: 'get',
    params: { id }
  });
}

export function editGoods(data) {
  return request({
    url: '/goods/update',
    method: 'post',
    data
  });
}

export function listCatAndBrand() {
  return request({
    url: '/goods/catAndBrand',
    method: 'get'
  });
}

export function optionDel(data) {
  return request({
    url: '/goods/option/delete',
    method: 'post',
    data
  });
}
export function optionDownLoad(data) {
  return request({
    url: '/goods/option/download',
    method: 'post',
    data
  });
}
export function optionList(data) {
  return request({
    url: '/goods/option/list',
    method: 'post',
    data
  });
}
export function optionUpload(data) {
  return request({
    url: '/goods/option/upload',
    method: 'post',
    data
  });
}
export function optionUse(data) {
  return request({
    url: '/goods/option/use',
    method: 'post',
    data
  });
}
export function optionUpdate(data) {
  return request({
    url: '/goods/option/update',
    method: 'post',
    data
  });
}
export function priceUpdate(data) {
  return request({
    url: '/goods/option/price/update',
    method: 'post',
    data
  });
}
export function configUpdate(data) {
  return request({
    url: '/goods/option/update',
    method: 'post',
    data
  });
}
export function spotUpdate(data) {
  return request({
    url: '/goods/product/spot/update',
    method: 'post',
    data
  });
}
