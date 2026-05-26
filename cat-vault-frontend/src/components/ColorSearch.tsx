import React, { useState } from 'react';
import { Input, Button, Space, Card, Row, Col, Empty } from 'antd';
import { BgColorsOutlined } from '@ant-design/icons';
import { searchPictureByColor, PictureVO } from '@/services/pictureService';
import { useNavigate } from 'react-router-dom';

const presetColors = [
  '#FF0000', '#FF6600', '#FFCC00', '#33CC33', '#0099FF',
  '#6633CC', '#FF3399', '#000000', '#666666', '#FFFFFF',
];

const ColorSearch: React.FC = () => {
  const [color, setColor] = useState('#FF0000');
  const [results, setResults] = useState<PictureVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const navigate = useNavigate();

  const handleSearch = async (searchColor?: string) => {
    const targetColor = searchColor || color;
    setLoading(true);
    setSearched(true);
    try {
      const res: any = await searchPictureByColor(targetColor);
      if (res.code === 0) {
        setResults(res.data || []);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card title={<Space><BgColorsOutlined />颜色搜索</Space>} size="small">
      <div style={{ marginBottom: 12 }}>
        <Space wrap>
          {presetColors.map(c => (
            <div
              key={c}
              onClick={() => { setColor(c); handleSearch(c); }}
              style={{
                width: 28, height: 28, background: c, borderRadius: 4, cursor: 'pointer',
                border: color === c ? '3px solid #1890ff' : '1px solid #ddd',
              }}
            />
          ))}
        </Space>
      </div>
      <Space>
        <Input
          type="color"
          value={color}
          onChange={e => setColor(e.target.value)}
          style={{ width: 50, padding: 2 }}
        />
        <Input value={color} onChange={e => setColor(e.target.value)} style={{ width: 100 }} />
        <Button type="primary" onClick={() => handleSearch()} loading={loading}>搜索</Button>
      </Space>

      {searched && (
        <div style={{ marginTop: 16 }}>
          {results.length === 0 ? <Empty description="未找到相似颜色的图片" /> : (
            <Row gutter={[8, 8]}>
              {results.slice(0, 8).map(pic => (
                <Col key={pic.id} span={6}>
                  <img
                    src={pic.thumbnailUrl || pic.url}
                    alt={pic.name}
                    style={{ width: '100%', height: 80, objectFit: 'cover', borderRadius: 4, cursor: 'pointer' }}
                    onClick={() => navigate(`/picture/${pic.id}`)}
                  />
                </Col>
              ))}
            </Row>
          )}
        </div>
      )}
    </Card>
  );
};

export default ColorSearch;
