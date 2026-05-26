import React, { useEffect, useState } from 'react';
import { List, Badge, Button, Empty, message, Space } from 'antd';
import { CheckOutlined } from '@ant-design/icons';
import { listMyMessages, markAsRead, MessageVO } from '@/services/messageService';

const MessagesPage: React.FC = () => {
  const [messages, setMessages] = useState<MessageVO[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchMessages();
  }, []);

  const fetchMessages = async () => {
    setLoading(true);
    try {
      const res: any = await listMyMessages();
      if (res.code === 0) setMessages(res.data || []);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkRead = async (id: string) => {
    const res: any = await markAsRead([id]);
    if (res.code === 0) {
      setMessages(prev => prev.map(m => m.id === id ? { ...m, hasRead: 1 } : m));
    }
  };

  const handleMarkAllRead = async () => {
    const unreadIds = messages.filter(m => m.hasRead === 0).map(m => m.id);
    if (unreadIds.length === 0) return;
    const res: any = await markAsRead(unreadIds);
    if (res.code === 0) {
      message.success('已全部标记为已读');
      setMessages(prev => prev.map(m => ({ ...m, hasRead: 1 })));
    }
  };

  const unreadCount = messages.filter(m => m.hasRead === 0).length;

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Space>
          <h2 style={{ margin: 0 }}>我的消息</h2>
          {unreadCount > 0 && <Badge count={unreadCount} />}
        </Space>
        {unreadCount > 0 && (
          <Button onClick={handleMarkAllRead}>全部已读</Button>
        )}
      </div>

      <List
        loading={loading}
        dataSource={messages}
        locale={{ emptyText: <Empty description="暂无消息" /> }}
        renderItem={(item) => (
          <List.Item
            actions={item.hasRead === 0 ? [
              <Button size="small" icon={<CheckOutlined />} onClick={() => handleMarkRead(item.id)}>标记已读</Button>
            ] : []}
          >
            <List.Item.Meta
              title={
                <Space>
                  {item.hasRead === 0 && <Badge status="processing" />}
                  <span style={{ fontWeight: item.hasRead === 0 ? 600 : 400 }}>{item.title}</span>
                </Space>
              }
              description={
                <div>
                  <div>{item.content}</div>
                  <div style={{ fontSize: 12, color: '#999', marginTop: 4 }}>{item.createTime}</div>
                </div>
              }
            />
          </List.Item>
        )}
      />
    </div>
  );
};

export default MessagesPage;
