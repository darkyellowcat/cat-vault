import React, { useEffect, useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu, Avatar, Dropdown, Space, Badge } from 'antd';
import {
  PictureOutlined,
  UploadOutlined,
  TeamOutlined,
  UserOutlined,
  LogoutOutlined,
  SettingOutlined,
  BellOutlined,
  MessageOutlined,
} from '@ant-design/icons';
import { getLoginUser, userLogout, LoginUserVO } from '@/services/userService';
import { getUnreadCount } from '@/services/messageService';

const { Header, Content, Sider } = Layout;

const BasicLayout: React.FC = () => {
  const [currentUser, setCurrentUser] = useState<LoginUserVO | null>(null);
  const [unreadCount, setUnreadCount] = useState(0);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    fetchCurrentUser();
  }, []);

  useEffect(() => {
    if (currentUser) fetchUnread();
    const timer = setInterval(() => { if (currentUser) fetchUnread(); }, 30000);
    return () => clearInterval(timer);
  }, [currentUser]);

  const fetchCurrentUser = async () => {
    try {
      const res: any = await getLoginUser();
      if (res.code === 0) {
        setCurrentUser(res.data);
      }
    } catch {
      navigate('/login');
    }
  };

  const fetchUnread = async () => {
    try {
      const res: any = await getUnreadCount();
      if (res.code === 0) setUnreadCount(res.data || 0);
    } catch {}
  };

  const handleLogout = async () => {
    await userLogout();
    setCurrentUser(null);
    navigate('/login');
  };

  const menuItems = [
    { key: '/', icon: <PictureOutlined />, label: '图库' },
    ...(currentUser?.userRole === 'admin'
      ? [{ key: '/upload', icon: <UploadOutlined />, label: '上传' }]
      : []),
    { key: '/spaces', icon: <TeamOutlined />, label: '空间' },
    { key: '/messages', icon: <MessageOutlined />, label: '消息' },
    ...(currentUser?.userRole === 'admin'
      ? [{ key: '/admin', icon: <SettingOutlined />, label: '管理' }]
      : []),
  ];

  const userMenuItems = [
    { key: 'logout', icon: <LogoutOutlined />, label: '退出登录', onClick: handleLogout },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 24px' }}>
        <div style={{ color: '#fff', fontSize: 18, fontWeight: 'bold', cursor: 'pointer' }} onClick={() => navigate('/')}>
          Cat Vault
        </div>
        <Space size="middle">
          <Badge count={unreadCount} size="small">
            <BellOutlined style={{ color: '#fff', fontSize: 18, cursor: 'pointer' }} onClick={() => navigate('/messages')} />
          </Badge>
          <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
            <Space style={{ cursor: 'pointer', color: '#fff' }}>
              <Avatar src={currentUser?.userAvatar} icon={<UserOutlined />} />
              <span>{currentUser?.userName || currentUser?.userAccount}</span>
            </Space>
          </Dropdown>
        </Space>
      </Header>
      <Layout>
        <Sider width={200} style={{ background: '#fff' }}>
          <Menu
            mode="inline"
            selectedKeys={[location.pathname]}
            items={menuItems}
            onClick={({ key }) => navigate(key)}
            style={{ height: '100%', borderRight: 0 }}
          />
        </Sider>
        <Content style={{ padding: 24, margin: 0, minHeight: 280 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default BasicLayout;
