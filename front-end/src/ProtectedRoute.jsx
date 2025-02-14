// ProtectedRoute.jsx
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useUserContext } from './UserContext';

function ProtectedRoute() {
  const { state, dispatch } = useUserContext();
  const location = useLocation();

  if (!exceptionCase(location) && (!state.userId || state.userId == 'null')) {
    return <Navigate to='/' replace />;
  }
  return <Outlet />;
}

export default ProtectedRoute;

// 예외적인 경우 아래에 추가
function exceptionCase(location) {
  if (
    location.pathname === '/feedback/favorite' &&
    location.search === '?process=signup'
  )
    return true;
}
