import { Navigate, useParams, useSearchParams } from 'react-router';

import { Button } from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { configQueryOptions, fetchServiceName } from '../../api';
import { LoginContainer } from '../../components';
import I18n, { tArray } from '../../locale/I18n.ts';
import { getProceedUrl } from '../../utils/sso.ts';
import styles from './AttributeMissing.module.scss';

export default function AttributeMissing() {
    const { id = '' } = useParams();
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
        return null;
    }

    const proceedUrl = getProceedUrl(searchParams, config?.magicLinkUrl ?? '');
    if (!proceedUrl) {
        return <Navigate to="/expired" replace />;
    }

    const serviceName = service.name;

    return (
        <LoginContainer
            title={I18n.t('attributeMissing.title')}
            headerAlign="center"
        >
            {tArray(
                'attributeMissing.info',
                (info, index) => (
                    <p key={index} className={styles.info}>
                        {info}
                    </p>
                ),
                { serviceName },
            )}
            <div className={styles.actions}>
                <Button
                    type="button"
                    variant="outline"
                    onClick={() => {
                        if (proceedUrl) {
                            window.location.href = proceedUrl;
                        }
                    }}
                >
                    {I18n.t('attributeMissing.proceed')}
                </Button>
                <Button
                    type="button"
                    className={styles.primary}
                    onClick={() => {
                        window.location.href = `/myconext/api/idp/oidc/account/${encodeURIComponent(id)}?forceAuth=true`;
                    }}
                    autoFocus
                >
                    {I18n.t('attributeMissing.retry')}
                </Button>
            </div>
        </LoginContainer>
    );
}
