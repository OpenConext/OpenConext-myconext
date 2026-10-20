import { Navigate, useLocation } from 'react-router';

export default function WebauthnRedirect() {
    const { search } = useLocation();
    return <Navigate to={{ pathname: '/security/webauthn', search }} replace />;
}
