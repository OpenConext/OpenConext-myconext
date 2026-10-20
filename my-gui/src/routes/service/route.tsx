import { Navigate, useLocation } from 'react-router';

export default function ServiceRedirect() {
    const { search } = useLocation();
    return <Navigate to={{ pathname: '/activity', search }} replace />;
}
