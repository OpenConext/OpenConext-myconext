import { Navigate, useLocation } from 'react-router';

// the passkey test redirects here, the passkey dialog lives on the security page
export default function CredentialRedirect() {
    const params = new URLSearchParams(useLocation().search);
    const search = new URLSearchParams();
    const id = params.get('id');
    if (id) {
        search.set('credential', id);
    }
    const success = params.get('success');
    if (success) {
        search.set('success', success);
    }
    return (
        <Navigate
            to={{ pathname: '/security', search: search.toString() }}
            replace
        />
    );
}
