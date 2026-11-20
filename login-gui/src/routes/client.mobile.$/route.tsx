import { Navigate, useLocation, useParams } from 'react-router';

import { InstallAppQr, isTouchDevice } from '@myconext/shared-gui';
import { Button } from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { configQueryOptions } from '../../api';
import { LoadingLoginContainer, LoginContainer } from '../../components';
import I18n from '../../locale/I18n.ts';
import styles from './route.module.scss';

const ACTIONS = [
    'account-linked',
    'add-password',
    'created',
    'eppn-already-linked',
    'subject-already-linked',
    'expired',
    'reset-password',
    'security',
    'update-email',
    'external-account-linked',
    'external-account-linked-error',
] as const;

const SAFE_ACTION = /^[a-z0-9-]+$/;

type Action = (typeof ACTIONS)[number];

const isAction = (value: string): value is Action =>
    ACTIONS.some((action) => action === value);

export default function RedirectMobileApp() {
    const { '*': action = '' } = useParams();
    const { search } = useLocation();
    const { data: config, isPending } = useQuery(configQueryOptions);

    const scope = `redirectMobileApp.${isAction(action) ? action : 'fallback'}`;

    if (isPending) {
        return <LoadingLoginContainer title={I18n.t(`${scope}.title`)} />;
    }

    if (!config?.mobileAppRedirect) {
        return <Navigate to="/expired" replace />;
    }

    const safeAction = SAFE_ACTION.test(action) ? action : '';
    const appUrl = `${config.mobileAppRedirect}/${safeAction}${search}`;
    const isMobile = isTouchDevice();

    return (
        <LoginContainer title={I18n.t(`${scope}.title`)}>
            <div className={styles.content}>
                <p>{I18n.t(`${scope}.${isMobile ? 'info' : 'infoDesktop'}`)}</p>
                {isMobile ? (
                    <Button
                        type="button"
                        onClick={() => {
                            window.location.href = appUrl;
                        }}
                        autoFocus
                    >
                        {I18n.t('redirectMobileApp.proceed')}
                    </Button>
                ) : (
                    <>
                        <p>{I18n.t('redirectMobileApp.qr')}</p>
                        <InstallAppQr
                            title={I18n.t('redirectMobileApp.qr')}
                            value={appUrl}
                            size={200}
                        />
                    </>
                )}
            </div>
        </LoginContainer>
    );
}
