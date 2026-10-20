import { Navigate, useLocation } from 'react-router';

export default function ExternalAccountLinkedErrorRedirect() {
    const { search } = useLocation();
    return (
        <Navigate
            to={{ pathname: '/identity/external-account-linked-error', search }}
            replace
        />
    );
}
