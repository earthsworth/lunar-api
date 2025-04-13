import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface UserState {
  id: string | null;
  username: string | null;
  uuid: string | null;
  roles: string[];
  logoColor: number;
}

const initialState: UserState = {
  id: null,
  username: null,
  uuid: null,
  roles: [],
  logoColor: 0xFFFFFF
};

const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    setUser(state, action: PayloadAction<UserState>) {
      return { ...state, ...action.payload };
    },

    clearUser() {
      return initialState;
    }
  }
});

export const { setUser, clearUser } = userSlice.actions;
export default userSlice.reducer;
