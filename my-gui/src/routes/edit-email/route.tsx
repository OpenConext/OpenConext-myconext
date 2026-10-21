import { Navigate } from 'react-router';

// the email change is a dialog on the security page
export default function EditEmailRedirect() {
    return (
        <Navigate
            to={{ pathname: '/security', search: '?editEmail=1' }}
            replace
        />
    );
}
