import { Navigate } from 'react-router';

// downloading the data and deleting the account are in the user menu
export default function AccountRedirect() {
    return <Navigate to={{ pathname: '/' }} replace />;
}
