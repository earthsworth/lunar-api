import { SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar';
import DashboardSideBar from '@/components/dashboard/dashboard-sidebar.tsx';
import { Outlet } from 'react-router-dom';

const DashboardLayout = () => {
  return (
    <SidebarProvider>
      <DashboardSideBar />
      <main>
        <SidebarTrigger />
        <Outlet />
      </main>
    </SidebarProvider>
  );
};

export default DashboardLayout;
