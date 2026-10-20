import { Navigate, useLocation } from 'react-router';

export default function ManageRedirect() {
    const { search } = useLocation();
    return <Navigate to={{ pathname: '/identity', search }} replace />;
}
