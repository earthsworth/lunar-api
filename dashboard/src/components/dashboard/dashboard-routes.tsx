import { Route, Routes, useNavigate } from 'react-router-dom';
import NotFound from '../NotFound.tsx';
import DashboardLayout from '@/components/dashboard/dashboard-layout.tsx';
import { selfInfo } from '@/api/user.ts';
import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { setUser } from '@/store/slices/userSlice.ts';
import { RootState } from '@/store/store.ts';
import HomePage from '@/components/dashboard/pages/home/home-page.tsx';
import JamsPage from '@/components/dashboard/pages/jams/jams-page.tsx';

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
          uuid: info.uuid,
          logoColor: info.logoColor
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
        <Route path="/" element={<HomePage />} />
        <Route path="/jams" element={<JamsPage />} />
      </Route>
    </Routes>
  );
};

export default DashboardRoutes;