import { Navigate, useSearchParams } from 'react-router';

import { Button } from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { configQueryOptions, fetchServiceName } from '../../api';
import I18n, { tArray } from '../../locale/I18n';
import { getProceedUrl } from '../../utils/sso';
import { LoadingLoginContainer } from '../Loading/LoadingLoginContainer.tsx';
import { LoginContainer } from '../LoginContainer/LoginContainer';
import styles from './LinkOutcome.module.scss';

interface LinkOutcomeProps {
    id: string;
    /** i18n scope containing `title` and an `info` string array (may use `{{serviceName}}`). */
    scope: string;
    /** Extra interpolation values for the `info` paragraphs. */
    options?: Record<string, string>;
    /** Where "retry" sends the user: back to the institution login or to the stepup alternatives. */
    retryTarget: 'institution' | 'stepup';
}

const getRetryUrl = (id: string, target: LinkOutcomeProps['retryTarget']) =>
    target === 'institution'
        ? `/myconext/api/idp/oidc/account/${encodeURIComponent(id)}?forceAuth=true`
        : `/stepup/${encodeURIComponent(id)}?retry=true`;

export const LinkOutcome = ({
    id,
    scope,
    options,
    retryTarget,
}: LinkOutcomeProps) => {
    const [searchParams] = useSearchParams();

    const { data: config, isPending: isConfigPending } =
        useQuery(configQueryOptions);

    const {
        data: service,
        isPending: isServicePending,
        isError,
    } = useQuery({
        queryKey: ['serviceName', id],
        queryFn: () => fetchServiceName(id),
        enabled: !!id,
    });

    if (!id || isError) {
        return <Navigate to="/expired" replace />;
    }

    if (isConfigPending || isServicePending) {
        return <LoadingLoginContainer title={I18n.t(`${scope}.title`)} />;
    }

    const proceedUrl = getProceedUrl(searchParams, config?.magicLinkUrl ?? '');
    if (!proceedUrl) {
        return <Navigate to="/expired" replace />;
    }

    return (
        <LoginContainer title={I18n.t(`${scope}.title`)} headerAlign="center">
            {tArray(
                `${scope}.info`,
                (info, index) => (
                    <p key={index} className={styles.info}>
                        {info}
                    </p>
                ),
                { ...options, serviceName: service.name },
            )}
            <div className={styles.actions}>
                <Button
                    type="button"
                    variant="outline"
                    onClick={() => {
                        window.location.href = proceedUrl;
                    }}
                >
                    {I18n.t('linkOutcome.proceed')}
                </Button>
                <Button
                    type="button"
                    className={styles.primary}
                    onClick={() => {
                        window.location.href = getRetryUrl(id, retryTarget);
                    }}
                    autoFocus
                >
                    {I18n.t('linkOutcome.retry')}
                </Button>
            </div>
        </LoginContainer>
    );
};
