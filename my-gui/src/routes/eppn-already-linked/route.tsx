import { Navigate, useLocation } from 'react-router';

export default function EppnAlreadyLinkedRedirect() {
    const { search } = useLocation();
    return (
        <Navigate
            to={{ pathname: '/identity/eppn-already-linked', search }}
            replace
        />
    );
}
