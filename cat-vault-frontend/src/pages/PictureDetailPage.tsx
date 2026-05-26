import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Descriptions, Image, Tag, Button, Space, message, Popconfirm } from 'antd';
import { ArrowLeftOutlined, DeleteOutlined, EditOutlined } from '@ant-design/icons';
import { getPictureVOById, deletePicture, PictureVO } from '@/services/pictureService';
import PictureEditModal from '@/components/PictureEditModal';
import request from '@/utils/request';

const PictureDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [picture, setPicture] = useState<PictureVO | null>(null);
  const [loading, setLoading] = useState(true);
  const [editVisible, setEditVisible] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (id) fetchPicture(id);
  }, [id]);

  const fetchPicture = async (pictureId: string) => {
    setLoading(true);
    try {
      const res: any = await getPictureVOById(pictureId);
      if (res.code === 0) {
        setPicture(res.data);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!picture) return;
    const res: any = await deletePicture({ id: picture.id });
    if (res.code === 0) {
      message.success('删除成功');
      navigate('/');
    } else {
      message.error(res.message || '删除失败');
    }
  };

  const handleEditImage = async (values: any): Promise<string | null> => {
    if (!picture) return null;
    const res: any = await request.post('/picture/edit/image', { pictureId: picture.id, ...values });
    if (res.code === 0) {
      return res.data;
    } else {
      message.error(res.message || '编辑失败');
      return null;
    }
  };

  if (loading) return <Card loading />;
  if (!picture) return <Card>图片不存在</Card>;

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>返回</Button>
        <Button icon={<EditOutlined />} onClick={() => setEditVisible(true)}>编辑图片</Button>
        <Popconfirm title="确定删除？" onConfirm={handleDelete}>
          <Button danger icon={<DeleteOutlined />}>删除</Button>
        </Popconfirm>
      </Space>

      <Card>
        <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
          <div style={{ flex: '1 1 400px' }}>
            <Image src={picture.url} alt={picture.name} style={{ maxWidth: '100%' }} />
          </div>
          <div style={{ flex: '1 1 300px' }}>
            <Descriptions title={picture.name} column={1} bordered size="small">
              <Descriptions.Item label="简介">{picture.introduction || '-'}</Descriptions.Item>
              <Descriptions.Item label="分类">{picture.category || '-'}</Descriptions.Item>
              <Descriptions.Item label="标签">
                {picture.tags?.map((t) => <Tag key={t} color="blue">{t}</Tag>) || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="尺寸">{picture.picWidth} x {picture.picHeight}</Descriptions.Item>
              <Descriptions.Item label="大小">{((picture.picSize || 0) / 1024).toFixed(1)} KB</Descriptions.Item>
              <Descriptions.Item label="格式">{picture.picFormat}</Descriptions.Item>
              <Descriptions.Item label="主色调">
                {picture.picColor && (
                  <Space>
                    <div style={{ width: 20, height: 20, background: picture.picColor, border: '1px solid #ddd' }} />
                    {picture.picColor}
                  </Space>
                )}
              </Descriptions.Item>
              <Descriptions.Item label="上传时间">{picture.createTime}</Descriptions.Item>
              <Descriptions.Item label="上传者">{picture.user?.userName || '-'}</Descriptions.Item>
            </Descriptions>
          </div>
        </div>
      </Card>

      <PictureEditModal
        visible={editVisible}
        pictureUrl={picture.url}
        onCancel={() => setEditVisible(false)}
        onSubmit={handleEditImage}
      />
    </div>
  );
};

export default PictureDetailPage;
