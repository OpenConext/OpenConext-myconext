import { Navigate } from 'react-router';

export default function WebauthnRedirect() {
    return <Navigate to={{ pathname: '/security' }} replace />;
}
