import request from '@/utils/request';

export interface LoginUserVO {
  id: string;
  userAccount: string;
  userName: string;
  userAvatar: string;
  userProfile: string;
  userRole: string;
  vipLevel: number;
  createTime: string;
}

export interface UserVO {
  id: string;
  userAccount: string;
  userName: string;
  userAvatar: string;
  userProfile: string;
  userRole: string;
  createTime: string;
}

export function userRegister(data: { userAccount: string; userPassword: string; checkPassword: string }) {
  return request.post('/user/register', data);
}

export function userLogin(data: { userAccount: string; userPassword: string }) {
  return request.post('/user/login', data);
}

export function getLoginUser() {
  return request.post('/user/get/login');
}

export function userLogout() {
  return request.post('/user/logout');
}

export function listUserVOByPage(data: any) {
  return request.post('/user/list/page/vo', data);
}

export function deleteUser(data: { id: string }) {
  return request.post('/user/delete', data);
}

export function updateUser(data: any) {
  return request.post('/user/update', data);
}
