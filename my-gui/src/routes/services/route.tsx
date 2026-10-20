import { Navigate, useLocation } from 'react-router';

export default function ServicesRedirect() {
    const { search } = useLocation();
    return <Navigate to={{ pathname: '/activity', search }} replace />;
}
