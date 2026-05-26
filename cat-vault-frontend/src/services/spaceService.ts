import request from '@/utils/request';

export function addSpace(data: { spaceName: string; spaceLevel: number; spaceType?: number }) {
  return request.post('/space/add', data);
}

export function deleteSpace(data: { id: string | number }) {
  return request.post('/space/delete', data);
}

export function updateSpace(data: any) {
  return request.post('/space/update', data);
}

export function getSpaceVOById(id: string | number) {
  return request.get('/space/get/vo', { params: { id } });
}

export function listSpaceVOByPage(data: any) {
  return request.post('/space/list/page/vo', data);
}

export function addSpaceMember(data: { spaceId: string | number; userId: string | number; spaceRole?: string }) {
  return request.post('/space/member/add', data);
}

export function removeSpaceMember(data: { spaceId: string | number; userId: string | number }) {
  return request.post('/space/member/remove', data);
}

export function listSpaceMembers(data: { spaceId: string | number }) {
  return request.post('/space/member/list', data);
}

export function listMySpaces() {
  return request.get('/space/my/list');
}
