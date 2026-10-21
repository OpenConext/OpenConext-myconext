import { Navigate } from 'react-router';

// deleting the account is a dialog in the user menu
export default function DeleteAccountRedirect() {
    return <Navigate to={{ pathname: '/' }} replace />;
}
