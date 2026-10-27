import { Spinner } from '@surfnet/curve-react';
import { useEffect } from 'react';
import { Outlet } from 'react-router';

import { redirectToLogin } from '../api';
import '../components/AuthLayout.scss';
import { PageFooter } from '../components/PageFooter';
import { TopNavigation } from '../components/TopNavigation';
import { UserFeedbackWidget } from '../components/UserFeedbackWidget';
import I18n from '../locale/I18n';
import { useConfigStore } from '../store/config';
import { loadUser, useUserStore } from '../store/me';

export default function ProtectedLayout() {
    const config = useConfigStore((state) => state.config);
    const configError = useConfigStore((state) => state.isError);
    const isAuthenticated = config?.isAuthenticated ?? false;

    const me = useUserStore((state) => state.user);
    const meError = useUserStore((state) => state.isError);

    // the user is fetched once, as soon as the config says it is logged in
    useEffect(() => {
        if (isAuthenticated) {
            void loadUser();
        }
    }, [isAuthenticated]);

    useEffect(() => {
        if (config && !isAuthenticated && config.loginUrl) {
            redirectToLogin(
                config.loginUrl,
                window.location.pathname + window.location.search,
            );
        }
    }, [config, isAuthenticated]);

    if (configError || meError) {
        return <div>{I18n.t('common.error')}</div>;
    }

    if (!isAuthenticated || !me) {
        return (
            <div
                className="auth-layout__loading"
                role="status"
                aria-label={I18n.t('common.loading')}
            >
                <Spinner />
            </div>
        );
    }

    return (
        <div className="auth-layout">
            <TopNavigation userName={me.chosenName} />
            <main className="auth-layout__main">
                <Outlet />
            </main>
            <PageFooter />
            <UserFeedbackWidget />
        </div>
    );
}
