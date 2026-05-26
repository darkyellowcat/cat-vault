import React, { useEffect, useState } from 'react';
import { Card, Input, Select, Tag, Pagination, Empty, Spin, Row, Col } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import { listPictureVOByPageWithCache, getTagCategory, PictureVO } from '@/services/pictureService';
import { useNavigate } from 'react-router-dom';
import ColorSearch from '@/components/ColorSearch';

const { Option } = Select;

const GalleryPage: React.FC = () => {
  const [pictures, setPictures] = useState<PictureVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [searchText, setSearchText] = useState('');
  const [category, setCategory] = useState<string>();
  const [categories, setCategories] = useState<string[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    fetchTagCategory();
  }, []);

  useEffect(() => {
    fetchPictures();
  }, [current, searchText, category]);

  const fetchTagCategory = async () => {
    const res: any = await getTagCategory();
    if (res.code === 0) {
      setCategories(res.data.categoryList || []);
    }
  };

  const fetchPictures = async () => {
    setLoading(true);
    try {
      const res: any = await listPictureVOByPageWithCache({
        current,
        pageSize: 12,
        searchText: searchText || undefined,
        category: category || undefined,
      });
      if (res.code === 0) {
        setPictures(res.data.records || []);
        setTotal(res.data.total || 0);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', gap: 12 }}>
        <Input.Search
          placeholder="搜索图片"
          prefix={<SearchOutlined />}
          onSearch={(v) => { setSearchText(v); setCurrent(1); }}
          style={{ width: 300 }}
          allowClear
        />
        <Select
          placeholder="选择分类"
          allowClear
          style={{ width: 150 }}
          onChange={(v) => { setCategory(v); setCurrent(1); }}
        >
          {categories.map((c) => (
            <Option key={c} value={c}>{c}</Option>
          ))}
        </Select>
      </div>

      <Spin spinning={loading}>
        {pictures.length === 0 ? (
          <Empty description="暂无图片" />
        ) : (
          <Row gutter={[16, 16]}>
            {pictures.map((pic) => (
              <Col key={pic.id} xs={24} sm={12} md={8} lg={6}>
                <Card
                  hoverable
                  cover={
                    <img
                      alt={pic.name}
                      src={pic.thumbnailUrl || pic.url}
                      style={{ height: 200, objectFit: 'cover' }}
                    />
                  }
                  onClick={() => navigate(`/picture/${pic.id}`)}
                >
                  <Card.Meta
                    title={pic.name}
                    description={
                      <div>
                        {pic.tags?.map((tag) => (
                          <Tag key={tag} color="blue">{tag}</Tag>
                        ))}
                      </div>
                    }
                  />
                </Card>
              </Col>
            ))}
          </Row>
        )}
      </Spin>

      <div style={{ marginTop: 24, textAlign: 'center' }}>
        <Pagination
          current={current}
          total={total}
          pageSize={12}
          onChange={(page) => setCurrent(page)}
          showTotal={(t) => `共 ${t} 张图片`}
        />
      </div>

      <div style={{ marginTop: 24 }}>
        <ColorSearch />
      </div>
    </div>
  );
};

export default GalleryPage;
