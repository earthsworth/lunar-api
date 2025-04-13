import { HashRouter, Route, Routes } from 'react-router-dom';
import RequireAuth from './RequireAuth.tsx';
import LoginPage from '@/components/login/login-page.tsx';
import DashboardRoutes from './dashboard/dashboard-routes.tsx';
import { ThemeProvider } from '@/components/theme-provider.tsx';

const App = () => {
  return (
    <ThemeProvider defaultTheme="system" storageKey="vite-ui-theme">
      <HashRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route
            path="*"
            element={
              <RequireAuth>
                <DashboardRoutes />
              </RequireAuth>
            }
          />
        </Routes>
      </HashRouter>
    </ThemeProvider>
  );
};

export default App;
