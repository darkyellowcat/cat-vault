import request from '@/utils/request';

export interface PictureVO {
  id: string;
  url: string;
  thumbnailUrl: string;
  name: string;
  introduction: string;
  tags: string[];
  category: string;
  picSize: number;
  picWidth: number;
  picHeight: number;
  picScale: number;
  picFormat: string;
  picColor: string;
  userId: string;
  spaceId: string;
  createTime: string;
  editTime: string;
  user: any;
}

export function listPictureVOByPage(data: any) {
  return request.post('/picture/list/page/vo', data);
}

export function listPictureVOByPageWithCache(data: any) {
  return request.post('/picture/list/page/vo/cache', data);
}

export function getPictureVOById(id: string) {
  return request.get('/picture/get/vo', { params: { id } });
}

export function uploadPicture(file: File, data?: any) {
  const formData = new FormData();
  formData.append('file', file);
  if (data?.id) formData.append('id', data.id);
  if (data?.spaceId) formData.append('spaceId', data.spaceId);
  if (data?.picName) formData.append('picName', data.picName);
  if (data?.introduction) formData.append('introduction', data.introduction);
  if (data?.category) formData.append('category', data.category);
  if (data?.tags) formData.append('tags', data.tags);
  return request.post('/picture/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

export function uploadPictureByUrl(data: { fileUrl: string; id?: number; picName?: string; introduction?: string; category?: string; tags?: string }) {
  return request.post('/picture/upload/url', data);
}

export function deletePicture(data: { id: string }) {
  return request.post('/picture/delete', data);
}

export function editPicture(data: any) {
  return request.post('/picture/edit', data);
}

export function doPictureReview(data: { id: string; reviewStatus: number; reviewMessage?: string }) {
  return request.post('/picture/review', data);
}

export function listPictureByPage(data: any) {
  return request.post('/picture/list/page', data);
}

export function getTagCategory() {
  return request.get('/picture/tag_category');
}

export function searchPictureByColor(picColor: string, spaceId?: string) {
  return request.get('/picture/search/color', { params: { picColor, spaceId } });
}
