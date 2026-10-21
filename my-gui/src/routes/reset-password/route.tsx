import { Navigate, useLocation } from 'react-router';

// the mailed link carries the hash `h`, the security page continues with it
export default function ResetPasswordRedirect() {
    const hash = new URLSearchParams(useLocation().search).get('h');
    return (
        <Navigate
            to={{
                pathname: '/security',
                search: hash ? `?passwordHash=${encodeURIComponent(hash)}` : '',
            }}
            replace
        />
    );
}
