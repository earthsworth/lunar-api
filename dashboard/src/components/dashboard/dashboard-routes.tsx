import { Route, Routes } from 'react-router-dom';
import NotFound from '../NotFound.tsx';
import DashboardLayout from '@/components/dashboard/layout.tsx';
import { dashboardRoutes } from '@/components/dashboard/routes.tsx';

const DashboardRoutes = () => {
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