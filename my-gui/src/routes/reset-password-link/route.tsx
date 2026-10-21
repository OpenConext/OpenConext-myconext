import { Navigate } from 'react-router';

// opens the password dialog on the security page
export default function ResetPasswordLinkRedirect() {
    return (
        <Navigate
            to={{ pathname: '/security', search: '?password=1' }}
            replace
        />
    );
}
