import { Navigate } from 'react-router';

export default function ResetPasswordRedirect() {
    return <Navigate to={{ pathname: '/security' }} replace />;
}
