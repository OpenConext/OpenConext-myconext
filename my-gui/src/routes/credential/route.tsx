import { Navigate, useLocation } from 'react-router';

export default function CredentialRedirect() {
    const { search } = useLocation();
    return (
        <Navigate to={{ pathname: '/security/credential', search }} replace />
    );
}
