import axios from 'axios';
import { message } from 'antd';

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  withCredentials: true,
});

request.interceptors.response.use(
  (response) => {
    const { data } = response;
    if (data.code === 40100) {
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login';
      }
    }
    return data;
  },
  (error) => {
    message.error(error.message || '网络错误');
    return Promise.reject(error);
  }
);

export default request;
