import React, { useEffect, useState } from 'react';
import { Card, Button, List, Modal, Form, Input, Select, Tag, message, Space } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { listMySpaces, addSpace } from '@/services/spaceService';
import { getLoginUser, LoginUserVO } from '@/services/userService';
import { useNavigate } from 'react-router-dom';

const SpacesPage: React.FC = () => {
  const [spaces, setSpaces] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [createVisible, setCreateVisible] = useState(false);
  const [currentUser, setCurrentUser] = useState<LoginUserVO | null>(null);
  const [form] = Form.useForm();
  const navigate = useNavigate();

  useEffect(() => {
    fetchSpaces();
    fetchUser();
  }, []);

  const fetchSpaces = async () => {
    setLoading(true);
    try {
      const res: any = await listMySpaces();
      if (res.code === 0) {
        setSpaces(res.data || []);
      }
    } finally {
      setLoading(false);
    }
  };

  const fetchUser = async () => {
    const res: any = await getLoginUser();
    if (res.code === 0) setCurrentUser(res.data);
  };

  const isVip = (currentUser?.vipLevel ?? 0) >= 1;

  const handleCreate = async (values: any) => {
    const res: any = await addSpace({ ...values, spaceType: 1 });
    if (res.code === 0) {
      message.success('创建成功');
      setCreateVisible(false);
      form.resetFields();
      fetchSpaces();
    } else {
      message.error(res.message || '创建失败');
    }
  };

  const levelMap: Record<number, { text: string; color: string }> = {
    0: { text: '免费版', color: 'default' },
    1: { text: '专业版', color: 'blue' },
    2: { text: '旗舰版', color: 'gold' },
  };

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2 style={{ margin: 0 }}>我的空间</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateVisible(true)}>
          创建空间
        </Button>
      </div>

      <List
        loading={loading}
        grid={{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 3 }}
        dataSource={spaces}
        renderItem={(item: any) => (
          <List.Item>
            <Card
              hoverable
              onClick={() => navigate(`/space/${item.space?.id || item.spaceId}`)}
            >
              <Card.Meta
                title={
                  <Space>
                    {item.space?.spaceName || '未命名空间'}
                    <Tag color={levelMap[item.space?.spaceLevel]?.color}>
                      {levelMap[item.space?.spaceLevel]?.text}
                    </Tag>
                  </Space>
                }
                description={`角色: ${item.spaceRole} | 图片: ${item.space?.totalCount || 0}`}
              />
            </Card>
          </List.Item>
        )}
      />

      <Modal
        title="创建空间"
        open={createVisible}
        onCancel={() => setCreateVisible(false)}
        onOk={() => form.submit()}
      >
        <Form form={form} onFinish={handleCreate} layout="vertical">
          <Form.Item name="spaceName" label="空间名称" rules={[{ required: true, message: '请输入空间名称' }]}>
            <Input placeholder="输入空间名称" maxLength={30} />
          </Form.Item>
          <Form.Item name="spaceLevel" label="空间级别" rules={[{ required: true, message: '请选择级别' }]}>
            <Select placeholder="选择级别">
              <Select.Option value={0}>免费版（100张 / 100MB）</Select.Option>
              {isVip && <Select.Option value={1}>专业版（1000张 / 1GB）</Select.Option>}
              {isVip && <Select.Option value={2}>旗舰版（10000张 / 10GB）</Select.Option>}
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default SpacesPage;
