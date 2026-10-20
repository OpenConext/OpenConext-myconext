import { Navigate, useLocation } from 'react-router';

export default function HomeRedirect() {
    const { search } = useLocation();
    return <Navigate to={{ pathname: '/', search }} replace />;
}
