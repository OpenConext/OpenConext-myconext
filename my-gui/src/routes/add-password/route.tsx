import { Navigate, useLocation } from 'react-router';

export default function AddPasswordRedirect() {
    const { search } = useLocation();
    return <Navigate to={{ pathname: '/security/password', search }} replace />;
}
