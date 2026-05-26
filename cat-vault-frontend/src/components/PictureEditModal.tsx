import React, { useState } from 'react';
import { Modal, Form, InputNumber, Input, Select, Button, Image, Space } from 'antd';

interface PictureEditModalProps {
  visible: boolean;
  pictureUrl: string;
  onCancel: () => void;
  onSubmit: (values: any) => Promise<string | null>;
}

const PictureEditModal: React.FC<PictureEditModalProps> = ({ visible, onCancel, onSubmit }) => {
  const [form] = Form.useForm();
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handlePreview = async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const url = await onSubmit(values);
      if (url) setPreviewUrl(url);
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setPreviewUrl(null);
    form.resetFields();
    onCancel();
  };

  return (
    <Modal title="图片编辑" open={visible} onCancel={handleClose} width={700} footer={[
      <Button key="cancel" onClick={handleClose}>关闭</Button>,
      <Button key="preview" type="primary" loading={loading} onClick={handlePreview}>生成预览</Button>,
    ]}>
      <Form form={form} layout="vertical">
        <Form.Item label="裁剪">
          <Space>
            <Form.Item name="cropX" noStyle><InputNumber placeholder="X" min={0} /></Form.Item>
            <Form.Item name="cropY" noStyle><InputNumber placeholder="Y" min={0} /></Form.Item>
            <Form.Item name="cropWidth" noStyle><InputNumber placeholder="宽" min={1} /></Form.Item>
            <Form.Item name="cropHeight" noStyle><InputNumber placeholder="高" min={1} /></Form.Item>
          </Space>
        </Form.Item>
        <Form.Item label="缩放">
          <Space>
            <Form.Item name="scaleWidth" noStyle><InputNumber placeholder="宽" min={1} /></Form.Item>
            <span>x</span>
            <Form.Item name="scaleHeight" noStyle><InputNumber placeholder="高" min={1} /></Form.Item>
          </Space>
        </Form.Item>
        <Form.Item name="rotate" label="旋转角度">
          <Select allowClear placeholder="选择角度">
            <Select.Option value={90}>90</Select.Option>
            <Select.Option value={180}>180</Select.Option>
            <Select.Option value={270}>270</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="format" label="输出格式">
          <Select allowClear placeholder="保持原格式">
            <Select.Option value="jpg">JPG</Select.Option>
            <Select.Option value="png">PNG</Select.Option>
            <Select.Option value="webp">WebP</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="watermarkText" label="文字水印">
          <Input placeholder="输入水印文字（可选）" />
        </Form.Item>
      </Form>

      {previewUrl && (
        <div style={{ marginTop: 16, textAlign: 'center' }}>
          <div style={{ marginBottom: 8, fontWeight: 500 }}>编辑预览</div>
          <Image src={previewUrl} style={{ maxHeight: 300 }} />
          <div style={{ marginTop: 8, fontSize: 12, color: '#999', wordBreak: 'break-all' }}>
            {previewUrl}
          </div>
        </div>
      )}
    </Modal>
  );
};

export default PictureEditModal;
