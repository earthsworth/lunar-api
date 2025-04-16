import { useSelector } from 'react-redux';
import type { RootState } from '@/store/store.ts';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card.tsx';
import LunarLogo from '@/components/lunar-logo.tsx';

const HomePage = () => {
  const uuid = useSelector((state: RootState) => state.user.uuid);
  const username = useSelector((state: RootState) => state.user.username);
  const roles = useSelector((state: RootState) => state.user.roles);
  const logoColor = useSelector((state: RootState) => state.user.logoColor);

  return (
    <div className="m-4">
      <Card>
        <CardHeader>
          <CardTitle>Your info</CardTitle>
        </CardHeader>
        <CardContent>
          <p>Username:
            <div className="inline bg-gray-400 m-1 rounded-sm">
              <LunarLogo logoColor={logoColor} className="inline align-middle size-4 ml-1" />
              <label className="p-1">{username}</label>
            </div>
          </p>
          <p>UUID: {uuid}</p>
          <p>Roles: {roles.join(', ')}</p>
        </CardContent>
        <CardFooter>
          <p>Change your password by using <strong>.passwd</strong> in lunar_bot.</p>
        </CardFooter>
      </Card>
    </div>
  );
};

export default HomePage;