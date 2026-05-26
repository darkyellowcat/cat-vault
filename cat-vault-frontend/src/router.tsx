import { createBrowserRouter, Navigate } from 'react-router-dom';
import BasicLayout from '@/layouts/BasicLayout';
import LoginPage from '@/pages/LoginPage';
import GalleryPage from '@/pages/GalleryPage';
import UploadPage from '@/pages/UploadPage';
import SpacesPage from '@/pages/SpacesPage';
import SpaceDetailPage from '@/pages/SpaceDetailPage';
import PictureDetailPage from '@/pages/PictureDetailPage';
import MessagesPage from '@/pages/MessagesPage';
import AdminPage from '@/pages/AdminPage';

const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/',
    element: <BasicLayout />,
    children: [
      { index: true, element: <GalleryPage /> },
      { path: 'upload', element: <UploadPage /> },
      { path: 'spaces', element: <SpacesPage /> },
      { path: 'space/:id', element: <SpaceDetailPage /> },
      { path: 'picture/:id', element: <PictureDetailPage /> },
      { path: 'messages', element: <MessagesPage /> },
      { path: 'admin', element: <AdminPage /> },
    ],
  },
  {
    path: '*',
    element: <Navigate to="/" replace />,
  },
]);

export default router;
