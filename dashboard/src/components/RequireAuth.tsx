import { Navigate, useLocation } from 'react-router-dom'
import { useSelector } from 'react-redux'
import type { RootState } from '../store/store'
import {ReactNode} from "react";

export default function RequireAuth({ children }: { children: ReactNode }) {
  const token = useSelector((state: RootState) => state.auth.token)
  const location = useLocation()

  if (!token) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return children
}