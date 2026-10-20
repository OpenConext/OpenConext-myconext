import { Navigate, useLocation } from 'react-router';

export default function DeleteAccountRedirect() {
    const { search } = useLocation();
    return <Navigate to={{ pathname: '/account/delete', search }} replace />;
}
