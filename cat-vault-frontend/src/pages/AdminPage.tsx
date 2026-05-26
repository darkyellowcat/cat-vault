import React, { useEffect, useState } from 'react';
import { Tabs, Table, Button, Tag, Space, message, Select, Image } from 'antd';
import { CheckOutlined, CloseOutlined, DeleteOutlined } from '@ant-design/icons';
import { listUserVOByPage, deleteUser } from '@/services/userService';
import { listPictureByPage, doPictureReview } from '@/services/pictureService';

const AdminPage: React.FC = () => {
  const [users, setUsers] = useState<any[]>([]);
  const [userTotal, setUserTotal] = useState(0);
  const [userPage, setUserPage] = useState(1);
  const [pictures, setPictures] = useState<any[]>([]);
  const [picTotal, setPicTotal] = useState(0);
  const [picPage, setPicPage] = useState(1);
  const [reviewStatus, setReviewStatus] = useState(0);

  useEffect(() => { fetchUsers(); }, [userPage]);
  useEffect(() => { fetchPictures(); }, [picPage, reviewStatus]);

  const fetchUsers = async () => {
    const res: any = await listUserVOByPage({ current: userPage, pageSize: 10 });
    if (res.code === 0) {
      setUsers(res.data.records || []);
      setUserTotal(res.data.total || 0);
    }
  };

  const fetchPictures = async () => {
    const res: any = await listPictureByPage({ current: picPage, pageSize: 10, reviewStatus });
    if (res.code === 0) {
      setPictures(res.data.records || []);
      setPicTotal(res.data.total || 0);
    }
  };

  const handleReview = async (id: string, status: number) => {
    const res: any = await doPictureReview({ id, reviewStatus: status });
    if (res.code === 0) {
      message.success(status === 1 ? '已通过' : '已拒绝');
      fetchPictures();
    } else {
      message.error(res.message);
    }
  };

  const handleDeleteUser = async (id: string) => {
    const res: any = await deleteUser({ id });
    if (res.code === 0) {
      message.success('已删除');
      fetchUsers();
    }
  };

  const userColumns = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    { title: '账号', dataIndex: 'userAccount' },
    { title: '昵称', dataIndex: 'userName' },
    { title: '角色', dataIndex: 'userRole', render: (v: string) => <Tag color={v === 'admin' ? 'red' : 'blue'}>{v}</Tag> },
    { title: '创建时间', dataIndex: 'createTime' },
    {
      title: '操作', render: (_: any, record: any) => (
        <Button danger size="small" icon={<DeleteOutlined />} onClick={() => handleDeleteUser(record.id)}>删除</Button>
      ),
    },
  ];

  const picColumns = [
    { title: '缩略图', dataIndex: 'thumbnailUrl', render: (v: string) => <Image src={v} width={60} height={60} style={{ objectFit: 'cover' }} /> },
    { title: '名称', dataIndex: 'name', ellipsis: true },
    { title: '尺寸', render: (_: any, r: any) => `${r.picWidth}x${r.picHeight}` },
    { title: '状态', dataIndex: 'reviewStatus', render: (v: number) => {
      const map: Record<number, { text: string; color: string }> = { 0: { text: '待审核', color: 'orange' }, 1: { text: '通过', color: 'green' }, 2: { text: '拒绝', color: 'red' } };
      return <Tag color={map[v]?.color}>{map[v]?.text}</Tag>;
    }},
    {
      title: '操作', render: (_: any, record: any) => (
        <Space>
          {record.reviewStatus === 0 && (
            <>
              <Button size="small" type="primary" icon={<CheckOutlined />} onClick={() => handleReview(record.id, 1)}>通过</Button>
              <Button size="small" danger icon={<CloseOutlined />} onClick={() => handleReview(record.id, 2)}>拒绝</Button>
            </>
          )}
        </Space>
      ),
    },
  ];

  const items = [
    {
      key: 'users',
      label: '用户管理',
      children: (
        <Table dataSource={users} columns={userColumns} rowKey="id"
          pagination={{ current: userPage, total: userTotal, pageSize: 10, onChange: setUserPage }} />
      ),
    },
    {
      key: 'pictures',
      label: '图片审核',
      children: (
        <div>
          <Select value={reviewStatus} onChange={setReviewStatus} style={{ marginBottom: 16, width: 120 }}>
            <Select.Option value={0}>待审核</Select.Option>
            <Select.Option value={1}>已通过</Select.Option>
            <Select.Option value={2}>已拒绝</Select.Option>
          </Select>
          <Table dataSource={pictures} columns={picColumns} rowKey="id"
            pagination={{ current: picPage, total: picTotal, pageSize: 10, onChange: setPicPage }} />
        </div>
      ),
    },
  ];

  return <Tabs items={items} />;
};

export default AdminPage;
