import { useEffect } from 'react';
import { Outlet } from 'react-router';

import { useQuery } from '@tanstack/react-query';

import { fetchConfig, fetchMe, redirectToLogin } from '../api';
import '../components/AuthLayout.scss';
import { PageFooter } from '../components/PageFooter';
import { TopNavigation } from '../components/TopNavigation';
import I18n from '../locale/I18n';

export default function ProtectedLayout() {
    const config = useQuery({ queryKey: ['config'], queryFn: fetchConfig });
    const isAuthenticated = config.data?.isAuthenticated ?? false;

    const me = useQuery({
        queryKey: ['me'],
        queryFn: fetchMe,
        enabled: isAuthenticated,
    });

    useEffect(() => {
        if (config.data && !isAuthenticated && config.data.loginUrl) {
            redirectToLogin(
                config.data.loginUrl,
                window.location.pathname + window.location.search,
            );
        }
    }, [config.data, isAuthenticated]);

    if (config.isError || me.isError) {
        return <div>{I18n.t('common.error')}</div>;
    }

    if (!isAuthenticated || !me.data) {
        return <div>{I18n.t('common.loading')}</div>;
    }

    return (
        <div className="auth-layout">
            <TopNavigation userName={me.data.chosenName} />
            <main className="auth-layout__main">
                <Outlet />
            </main>
            <PageFooter />
        </div>
    );
}
