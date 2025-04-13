import { Home, Music } from 'lucide-react';
import HomePage from '@/components/dashboard/pages/home/home-page.tsx';

export const dashboardRoutes = [
  {
    title: 'Home',
    path: '/',
    icon: <Home />,
    page: <HomePage />
  },
  {
    title: 'Upload Songs',
    path: '/song',
    icon: <Music />,
    page: <h1>Jams</h1>
  }
];