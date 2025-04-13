import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem
} from '@/components/ui/sidebar';
import { dashboardRoutes } from '@/components/dashboard/routes.tsx';
import { Link, useNavigate } from 'react-router-dom';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu.tsx';
import { ChevronUp } from 'lucide-react';
import { useDispatch, useSelector } from 'react-redux';
import type { RootState } from '@/store/store.ts';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar.tsx';
import { logout } from '@/api/user.ts';
import { clearAuth } from '@/store/slices/authSlice.ts';


const DashboardSideBar = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const uuid = useSelector((state: RootState) => state.user.uuid);
  const username = useSelector((state: RootState) => state.user.username);

  const handleSignOut = async () => {
    try {
      await logout(); // post logout
    } catch (err) {
      console.error('Failed to revoke token', err);
    } finally {
      // clear auth data
      dispatch(clearAuth());
      // navigate to login page
      navigate('/login', {
        replace: true
      });
    }
  };

  return (
    <Sidebar>
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel>LunarCN</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {dashboardRoutes.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild>
                    <Link to={item.path}>
                      {item.icon}
                      <span>{item.title}</span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>


      <SidebarFooter>
        <SidebarMenu>
          <SidebarMenuItem>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <SidebarMenuButton>
                  <Avatar>
                    <AvatarImage src={`https://skins.mcstats.com/skull/${uuid}`} />
                    <AvatarFallback>{username?.substring(0, 2)}</AvatarFallback>
                  </Avatar> {username}
                  <ChevronUp className="ml-auto" />
                </SidebarMenuButton>
              </DropdownMenuTrigger>
              <DropdownMenuContent
                side="top"
                className="w-[--radix-popper-anchor-width]"
              >
                <DropdownMenuItem>
                  <span>Account</span>
                </DropdownMenuItem>
                <DropdownMenuItem onClick={handleSignOut}>
                  <span>Sign out</span>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarFooter>
    </Sidebar>
  );
};

export default DashboardSideBar;