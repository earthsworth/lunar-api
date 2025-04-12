import { Route, Routes } from "react-router-dom";
import NotFound from "./NotFound.tsx";

const DashboardRoutes = () => {
  return (
    <Routes>
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
};

export default DashboardRoutes;