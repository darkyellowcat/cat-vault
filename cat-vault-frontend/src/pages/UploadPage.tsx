import React, { useState } from 'react';
import { Card, Upload, Button, Input, Select, Form, message, Tabs } from 'antd';
import { InboxOutlined, LinkOutlined } from '@ant-design/icons';
import { uploadPicture, uploadPictureByUrl } from '@/services/pictureService';

const { Dragger } = Upload;

const CATEGORIES = ['模板', '电商', '表情包', '素材', '海报'];
const TAGS = ['热门', '搞笑', '生活', '高清', '艺术', '校园', '背景', '简历', '创意'];

const UploadPage: React.FC = () => {
  const [urlValue, setUrlValue] = useState('');
  const [uploading, setUploading] = useState(false);
  const [fileForm] = Form.useForm();
  const [urlForm] = Form.useForm();

  const handleFileUpload = async (options: any) => {
    const { file, onSuccess, onError } = options;
    try {
      const values = fileForm.getFieldsValue();
      const data: any = {};
      if (values.picName) data.picName = values.picName;
      if (values.introduction) data.introduction = values.introduction;
      if (values.category) data.category = values.category;
      if (values.tags?.length) data.tags = JSON.stringify(values.tags);
      const res: any = await uploadPicture(file, data);
      if (res.code === 0) {
        message.success('上传成功');
        onSuccess(res.data);
      } else {
        message.error(res.message || '上传失败');
        onError(new Error(res.message));
      }
    } catch (err: any) {
      message.error('上传失败');
      onError(err);
    }
  };

  const handleUrlUpload = async () => {
    if (!urlValue.trim()) {
      message.warning('请输入图片 URL');
      return;
    }
    setUploading(true);
    try {
      const values = urlForm.getFieldsValue();
      const data: any = { fileUrl: urlValue };
      if (values.picName) data.picName = values.picName;
      if (values.introduction) data.introduction = values.introduction;
      if (values.category) data.category = values.category;
      if (values.tags?.length) data.tags = JSON.stringify(values.tags);
      const res: any = await uploadPictureByUrl(data);
      if (res.code === 0) {
        message.success('上传成功');
        setUrlValue('');
        urlForm.resetFields();
      } else {
        message.error(res.message || '上传失败');
      }
    } finally {
      setUploading(false);
    }
  };

  const items = [
    {
      key: 'file',
      label: '文件上传',
      children: (
        <div>
          <Form form={fileForm} layout="vertical" style={{ maxWidth: 500, marginBottom: 16 }}>
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
          <Dragger customRequest={handleFileUpload} accept="image/*" multiple={false}>
            <p className="ant-upload-drag-icon"><InboxOutlined /></p>
            <p className="ant-upload-text">点击或拖拽图片到此区域上传</p>
            <p className="ant-upload-hint">支持 JPG、PNG、WebP 格式，最大 2MB</p>
          </Dragger>
        </div>
      ),
    },
    {
      key: 'url',
      label: 'URL 上传',
      children: (
        <div style={{ maxWidth: 500 }}>
          <Form form={urlForm} layout="vertical">
            <Form.Item name="picName" label="图片名称">
              <Input placeholder="留空则自动提取" maxLength={128} />
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
          <Input
            prefix={<LinkOutlined />}
            placeholder="输入图片 URL"
            value={urlValue}
            onChange={(e) => setUrlValue(e.target.value)}
            style={{ marginBottom: 16 }}
          />
          <Button type="primary" onClick={handleUrlUpload} loading={uploading}>
            上传
          </Button>
        </div>
      ),
    },
  ];

  return (
    <Card title="上传图片">
      <Tabs items={items} />
    </Card>
  );
};

export default UploadPage;
