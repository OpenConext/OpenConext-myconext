import { useEffect } from 'react';
import { Outlet } from 'react-router';

import { useQuery } from '@tanstack/react-query';

import { fetchConfig, redirectToLogin } from '../api';

export default function ProtectedLayout() {
    const { data, isLoading, isError } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });

    const isAuthenticated = data?.isAuthenticated ?? false;

    useEffect(() => {
        if (data && !isAuthenticated && data.loginUrl) {
            redirectToLogin(data.loginUrl, window.location.pathname);
        }
    }, [data, isAuthenticated]);

    if (isLoading) {
        return <div>Loading...</div>;
    }

    if (isError) {
        return <div>Error!</div>;
    }

    if (!isAuthenticated) {
        return <div>Redirecting to login...</div>;
    }

    return <Outlet />;
}
