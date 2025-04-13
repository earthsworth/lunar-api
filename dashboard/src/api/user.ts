import axiosInstance from './axiosInstance.ts';
import { AuthorizeVO, RestBean, UserVO } from './entity.ts';

export const login = async (username: string, password: string) => {
  const params = new URLSearchParams();
  params.append('username', username);
  params.append('password', password);

  const response = await axiosInstance.post<RestBean<AuthorizeVO>>('/user/login', params, {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  });

  return response.data;
};

export const logout = async () => {
  await axiosInstance.post<RestBean<AuthorizeVO>>('/user/logout');
};

export const selfInfo = async () => {
  const response = await axiosInstance.get<RestBean<UserVO>>('/user');
  return response.data;
};