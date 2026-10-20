import { Navigate, useLocation } from 'react-router';

export default function ResetPasswordRedirect() {
    const { search } = useLocation();
    return <Navigate to={{ pathname: '/security/password', search }} replace />;
}
