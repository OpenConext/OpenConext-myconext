import { Navigate } from 'react-router';

export default function ResetPasswordLinkRedirect() {
    return <Navigate to={{ pathname: '/security' }} replace />;
}
