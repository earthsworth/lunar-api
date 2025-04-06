import { createSlice, PayloadAction } from '@reduxjs/toolkit'

interface AuthState {
  token: string | null
  tokenExpiry: number | null
}

const initialState: AuthState = {
  token: null,
  tokenExpiry: null
}

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setAuth(state, action: PayloadAction<{ token: string; tokenExpiry: number }>) {
      state.token = action.payload.token
      state.tokenExpiry = action.payload.tokenExpiry
    },
    clearAuth(state) {
      state.token = null
      state.tokenExpiry = null
    }
  }
})

export const { setAuth, clearAuth } = authSlice.actions
export default authSlice.reducer
