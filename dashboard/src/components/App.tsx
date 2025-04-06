import {HashRouter, Route, Routes} from "react-router-dom";
import RequireAuth from "./RequireAuth.tsx";
import {DashboardRoutes} from "./DashboardRoutes.tsx";
import {LoginPage} from "./LoginPage/LoginPage.tsx";

function App() {

  return (
    <HashRouter>
      <Routes>
        <Route path="/login" element={<LoginPage/>}/>

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
  );
}

export default App
