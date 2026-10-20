import { Navigate, useLocation } from 'react-router';

export default function AttributeMissingRedirect() {
    const { search } = useLocation();
    return (
        <Navigate
            to={{ pathname: '/identity/attribute-missing', search }}
            replace
        />
    );
}
