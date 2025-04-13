import { Route, Routes, useNavigate } from 'react-router-dom';
import NotFound from '../NotFound.tsx';
import DashboardLayout from '@/components/dashboard/layout.tsx';
import { dashboardRoutes } from '@/components/dashboard/routes.tsx';
import { selfInfo } from '@/api/user.ts';
import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { setUser } from '@/store/slices/userSlice.ts';
import { RootState } from '@/store/store.ts';

const DashboardRoutes = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const token = useSelector((state: RootState) => state.auth.token);
  const tokenExpiry = useSelector((state: RootState) => state.auth.tokenExpiry);

  useEffect(() => {
    const checkAuth = async () => {
      const now = Date.now();
      if (!token || (tokenExpiry && tokenExpiry <= now)) {
        navigate('/login');
        return;
      }

      try {
        const response = await selfInfo();
        const info = response.data!;
        dispatch(setUser({
          id: info.id,
          roles: info.roles,
          username: info.username,
          uuid: info.uuid
        }));
      } catch (err) {
        console.error('Failed to fetch user info', err);
        navigate('/login');
      }
    };

    checkAuth();
  }, [token, tokenExpiry]);

  return (
    <Routes>
      <Route path="*" element={<NotFound />} />
      <Route path="/" element={<DashboardLayout />}>
        {dashboardRoutes.map(route => (
          <Route path={route.path} element={route.page} />
        ))}
      </Route>
    </Routes>
  );
};

export default DashboardRoutes;