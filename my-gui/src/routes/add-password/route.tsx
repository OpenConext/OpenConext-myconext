import { Navigate } from 'react-router';

export default function AddPasswordRedirect() {
    return <Navigate to={{ pathname: '/security' }} replace />;
}
