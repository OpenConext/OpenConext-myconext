import { Outlet } from 'react-router';

// Todo replace with hook from config
const isAuthenticated = true;

export default function ProtectedLayout() {
    if (!isAuthenticated) {
        // Todo trigger redirect to login page
        return <h1>You are not authenticated</h1>;
    }

    return <Outlet />;
}
