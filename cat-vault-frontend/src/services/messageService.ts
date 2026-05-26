import request from '@/utils/request';

export interface MessageVO {
  id: string;
  userId: string;
  title: string;
  content: string;
  hasRead: number;
  createTime: string;
}

export function listMyMessages() {
  return request.get('/message/my/list');
}

export function getUnreadCount() {
  return request.get('/message/my/unread/count');
}

export function markAsRead(messageIds: string[]) {
  return request.post('/message/read', messageIds);
}
