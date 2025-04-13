import { useSelector } from 'react-redux';
import type { RootState } from '@/store/store.ts';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar.tsx';
import { SidebarMenuButton } from '@/components/ui/sidebar.tsx';

const UserInfo = () => {
  const uuid = useSelector((state: RootState) => state.user.uuid);
  const username = useSelector((state: RootState) => state.user.username);

  return (
    <SidebarMenuButton className="flex flex-row gap-2 items-center select-none">
      <Avatar>
        <AvatarImage src={`https://skins.mcstats.com/skull/${uuid}`} />
        <AvatarFallback>{username?.substring(0, 2)}</AvatarFallback>
      </Avatar>
      <p className="text-center">{username}</p>
      {/*  TODO refactor*/}
    </SidebarMenuButton>
  );
};

export default UserInfo;