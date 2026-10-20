import { Navigate, useLocation } from 'react-router';

export default function ResetPasswordLinkRedirect() {
    const { search } = useLocation();
    return <Navigate to={{ pathname: '/security/password', search }} replace />;
}
