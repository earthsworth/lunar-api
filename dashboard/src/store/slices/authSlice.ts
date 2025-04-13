import { createSlice, PayloadAction } from "@reduxjs/toolkit";

interface AuthState {
  token: string | null;
  tokenExpiry: number | null;
}

const initialState: AuthState = {
  token: null,
  tokenExpiry: null
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    setAuth(state, action: PayloadAction<AuthState>) {
      return { ...state, ...action.payload };
    },
    clearAuth() {
      return initialState;
    }
  }
});

export const { setAuth, clearAuth } = authSlice.actions;
export default authSlice.reducer;
