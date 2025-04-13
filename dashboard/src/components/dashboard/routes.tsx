import { Home, Music } from "lucide-react";

export const dashboardRoutes = [
  {
    title: "Home",
    path: "/",
    icon: <Home />,
    page: <h1>Home</h1>
  },
  {
    title: "Upload Songs",
    path: "/song",
    icon: <Music />,
    page: <h1>Jams</h1>
  }
];