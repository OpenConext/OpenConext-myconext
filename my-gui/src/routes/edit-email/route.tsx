import { Navigate, useLocation } from 'react-router';

export default function EditEmailRedirect() {
    const { search } = useLocation();
    return (
        <Navigate to={{ pathname: '/identity/edit-email', search }} replace />
    );
}
