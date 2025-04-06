import {Route, Routes} from "react-router-dom";
import {NotFound} from "./NotFound.tsx";

export function DashboardRoutes() {
  return (
    <Routes>
      <Route path="*" element={<NotFound/>}/>
    </Routes>
  );
}