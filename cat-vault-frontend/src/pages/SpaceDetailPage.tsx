import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Card, Row, Col, Tag, Empty, Button, Modal, Form, Input, Select, Table, Progress, Upload, message, Pagination } from 'antd';
import { UserAddOutlined, DeleteOutlined, UploadOutlined } from '@ant-design/icons';
import { getSpaceVOById, listSpaceMembers, addSpaceMember, removeSpaceMember, deleteSpace } from '@/services/spaceService';
import { listPictureVOByPage, uploadPicture, PictureVO } from '@/services/pictureService';
import { useNavigate } from 'react-router-dom';

const CATEGORIES = ['模板', '电商', '表情包', '素材', '海报'];
const TAGS = ['热门', '搞笑', '生活', '高清', '艺术', '校园', '背景', '简历', '创意'];

const SpaceDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [space, setSpace] = useState<any>(null);
  const [members, setMembers] = useState<any[]>([]);
  const [pictures, setPictures] = useState<PictureVO[]>([]);
  const [picTotal, setPicTotal] = useState(0);
  const [picPage, setPicPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [memberModalVisible, setMemberModalVisible] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadModalVisible, setUploadModalVisible] = useState(false);
  const [uploadFile, setUploadFile] = useState<File | null>(null);
  const [uploadForm] = Form.useForm();
  const [form] = Form.useForm();
  const navigate = useNavigate();

  useEffect(() => {
    if (id) {
      fetchSpace(id);
      fetchMembers(id);
      fetchPictures(id, 1);
    }
  }, [id]);

  useEffect(() => {
    if (id) fetchPictures(id, picPage);
  }, [picPage]);

  const fetchSpace = async (spaceId: string) => {
    setLoading(true);
    try {
      const res: any = await getSpaceVOById(spaceId);
      if (res.code === 0) setSpace(res.data);
    } finally {
      setLoading(false);
    }
  };

  const fetchMembers = async (spaceId: string) => {
    const res: any = await listSpaceMembers({ spaceId });
    if (res.code === 0) setMembers(res.data || []);
  };

  const fetchPictures = async (spaceId: string, page: number) => {
    const res: any = await listPictureVOByPage({ current: page, pageSize: 12, spaceId });
    if (res.code === 0) {
      setPictures(res.data.records || []);
      setPicTotal(res.data.total || 0);
    }
  };

  const handleAddMember = async (values: any) => {
    const res: any = await addSpaceMember({ spaceId: id!, ...values });
    if (res.code === 0) {
      message.success('添加成功');
      setMemberModalVisible(false);
      form.resetFields();
      fetchMembers(id!);
    } else {
      message.error(res.message || '添加失败');
    }
  };

  const handleRemoveMember = async (userId: string) => {
    const res: any = await removeSpaceMember({ spaceId: id!, userId });
    if (res.code === 0) {
      message.success('已移除');
      fetchMembers(id!);
    }
  };

  const handleUpload = async () => {
    if (!uploadFile) {
      message.warning('请先选择图片');
      return;
    }
    setUploading(true);
    try {
      const values = uploadForm.getFieldsValue();
      const data: any = { spaceId: id };
      if (values.picName) data.picName = values.picName;
      if (values.introduction) data.introduction = values.introduction;
      if (values.category) data.category = values.category;
      if (values.tags?.length) data.tags = JSON.stringify(values.tags);
      const res: any = await uploadPicture(uploadFile, data);
      if (res.code === 0) {
        message.success('上传成功');
        setUploadModalVisible(false);
        setUploadFile(null);
        uploadForm.resetFields();
        fetchPictures(id!, picPage);
        fetchSpace(id!);
      } else {
        message.error(res.message || '上传失败');
      }
    } finally {
      setUploading(false);
    }
  };

  const handleDeleteSpace = () => {
    Modal.confirm({
      title: '确认删除空间',
      content: '删除后空间内的图片关联将丢失，此操作不可恢复。',
      okText: '删除',
      okType: 'danger',
      onOk: async () => {
        const res: any = await deleteSpace({ id: id! });
        if (res.code === 0) {
          message.success('空间已删除');
          navigate('/spaces');
        } else {
          message.error(res.message || '删除失败');
        }
      },
    });
  };

  if (loading) return <Card loading />;
  if (!space) return <Card>空间不存在</Card>;

  const usagePercent = space.maxSize > 0 ? Math.round((space.totalSize / space.maxSize) * 100) : 0;
  const countPercent = space.maxCount > 0 ? Math.round((space.totalCount / space.maxCount) * 100) : 0;

  const memberColumns = [
    { title: '用户', dataIndex: ['user', 'userName'], render: (v: string) => v || '-' },
    { title: '账号', dataIndex: ['user', 'userAccount'] },
    { title: '角色', dataIndex: 'spaceRole', render: (v: string) => {
      const colorMap: Record<string, string> = { admin: 'red', editor: 'blue', viewer: 'default' };
      return <Tag color={colorMap[v]}>{v}</Tag>;
    }},
    { title: '操作', render: (_: any, record: any) => (
      <Button size="small" danger icon={<DeleteOutlined />} onClick={() => handleRemoveMember(record.userId)}>移除</Button>
    )},
  ];

  return (
    <div>
      <Card title={space.spaceName} extra={
        <div style={{ display: 'flex', gap: 8 }}>
          <Button type="primary" icon={<UploadOutlined />} onClick={() => setUploadModalVisible(true)}>上传图片</Button>
          <Button icon={<UserAddOutlined />} onClick={() => setMemberModalVisible(true)}>添加成员</Button>
          <Button danger icon={<DeleteOutlined />} onClick={handleDeleteSpace}>删除空间</Button>
        </div>
      }>
        <Row gutter={24}>
          <Col span={8}>
            <div>容量使用</div>
            <Progress percent={usagePercent} size="small" />
            <div style={{ fontSize: 12, color: '#999' }}>
              {(space.totalSize / 1024 / 1024).toFixed(1)}MB / {(space.maxSize / 1024 / 1024).toFixed(0)}MB
            </div>
          </Col>
          <Col span={8}>
            <div>图片数量</div>
            <Progress percent={countPercent} size="small" />
            <div style={{ fontSize: 12, color: '#999' }}>
              {space.totalCount} / {space.maxCount}
            </div>
          </Col>
          <Col span={8}>
            <div>成员数: {members.length}</div>
            <div style={{ fontSize: 12, color: '#999' }}>创建者: {space.user?.userName || '-'}</div>
          </Col>
        </Row>
      </Card>

      <Card title="成员管理" style={{ marginTop: 16 }}>
        <Table dataSource={members} columns={memberColumns} rowKey="id" pagination={false} size="small" />
      </Card>

      <Card title="空间图片" style={{ marginTop: 16 }}>
        {pictures.length === 0 ? <Empty description="暂无图片" /> : (
          <Row gutter={[16, 16]}>
            {pictures.map((pic) => (
              <Col key={pic.id} xs={24} sm={12} md={8} lg={6}>
                <Card hoverable cover={
                  <img alt={pic.name} src={pic.thumbnailUrl || pic.url} style={{ height: 160, objectFit: 'cover' }} />
                } onClick={() => navigate(`/picture/${pic.id}`)}>
                  <Card.Meta title={pic.name} />
                </Card>
              </Col>
            ))}
          </Row>
        )}
        <div style={{ marginTop: 16, textAlign: 'center' }}>
          <Pagination current={picPage} total={picTotal} pageSize={12} onChange={setPicPage} />
        </div>
      </Card>

      <Modal title="添加成员" open={memberModalVisible} onCancel={() => setMemberModalVisible(false)} onOk={() => form.submit()}>
        <Form form={form} onFinish={handleAddMember} layout="vertical">
          <Form.Item name="userId" label="用户 ID" rules={[{ required: true, message: '请输入用户 ID' }]}>
            <Input placeholder="输入要添加的用户 ID" />
          </Form.Item>
          <Form.Item name="spaceRole" label="角色" initialValue="viewer">
            <Select>
              <Select.Option value="viewer">查看者</Select.Option>
              <Select.Option value="editor">编辑者</Select.Option>
              <Select.Option value="admin">管理员</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="上传图片"
        open={uploadModalVisible}
        onCancel={() => { setUploadModalVisible(false); setUploadFile(null); uploadForm.resetFields(); }}
        onOk={handleUpload}
        confirmLoading={uploading}
        okText="上传"
      >
        <Form form={uploadForm} layout="vertical">
          <Form.Item label="选择图片" required>
            <Upload
              beforeUpload={(file) => { setUploadFile(file); return false; }}
              onRemove={() => setUploadFile(null)}
              fileList={uploadFile ? [{ uid: '-1', name: uploadFile.name, status: 'done' }] : []}
              accept="image/*"
              maxCount={1}
            >
              <Button icon={<UploadOutlined />}>选择文件</Button>
            </Upload>
          </Form.Item>
          <Form.Item name="picName" label="图片名称">
            <Input placeholder="留空则使用文件名" maxLength={128} />
          </Form.Item>
          <Form.Item name="introduction" label="简介">
            <Input.TextArea placeholder="图片简介（可选）" maxLength={512} rows={2} />
          </Form.Item>
          <Form.Item name="category" label="分类">
            <Select placeholder="选择分类（可选）" allowClear>
              {CATEGORIES.map(c => <Select.Option key={c} value={c}>{c}</Select.Option>)}
            </Select>
          </Form.Item>
          <Form.Item name="tags" label="标签">
            <Select mode="multiple" placeholder="选择标签（可选）" allowClear>
              {TAGS.map(t => <Select.Option key={t} value={t}>{t}</Select.Option>)}
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default SpaceDetailPage;
