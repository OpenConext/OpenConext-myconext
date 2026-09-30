import { Outlet } from 'react-router';

import { useQuery } from '@tanstack/react-query';

import { fetchConfig } from '../../api';

export default function ProtectedLayout() {
    const { data, isLoading, isError } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });

    if (isLoading) {
        return <div>Loading...</div>;
    }

    if (isError) {
        return <div>Error!</div>;
    }

    const isAuthenticated = data?.isAuthenticated ?? false;
    // const isAuthenticated = true;
    // const isAuthenticated = false;

    if (!isAuthenticated) {
        // Todo trigger redirect to login page
        return <h1>You are not authenticated</h1>;
    }

    return <Outlet />;
}
