import { Navigate, useSearchParams } from 'react-router';

import { Button } from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import {
    configQueryOptions,
    useServiceNameByHash,
    useUserInfoByHash,
} from '../../api';
import { LoadingLoginContainer, LoginContainer } from '../../components';
import I18n from '../../locale/I18n.ts';
import { formatDateOfBirth } from '../../utils/format.ts';
import { getProceedUrl } from '../../utils/sso.ts';
import styles from './route.module.scss';

export default function ConfirmExternalStepup() {
    const [searchParams] = useSearchParams();
    const hash = searchParams.get('h') ?? '';

    const { data: config, isPending: isConfigPending } =
        useQuery(configQueryOptions);
    const {
        data: service,
        isPending: isServicePending,
        isError: isServiceError,
    } = useServiceNameByHash(hash);
    const {
        data: userInfo,
        isPending: isUserPending,
        isError: isUserError,
    } = useUserInfoByHash(hash);

    if (!hash || isServiceError || isUserError) {
        return <Navigate to="/expired" replace />;
    }

    if (isConfigPending || isServicePending || isUserPending) {
        return <LoadingLoginContainer title={I18n.t('confirmStepup.title')} />;
    }

    const proceedUrl = getProceedUrl(searchParams, config?.magicLinkUrl ?? '');
    const account = userInfo.externalLinkedAccounts?.[0];
    if (!proceedUrl || !account) {
        return <Navigate to="/expired" replace />;
    }

    const issuerName = account.issuer?.name ?? '';
    const institution = I18n.t(`confirmExternalStepup.issuers.${issuerName}`, {
        defaultValue: issuerName,
    });

    return (
        <LoginContainer
            title={I18n.t('confirmExternalStepup.title', { institution })}
            description={I18n.t('confirmExternalStepup.description')}
            headerAlign="start"
        >
            <dl className={styles.fields}>
                {/* iDIN does not deliver a given name */}
                {account.idpScoping !== 'idin' && account.firstName && (
                    <div className={styles.field}>
                        <dt className={styles.label}>
                            {I18n.t('confirmExternalStepup.givenName')}
                        </dt>
                        <dd className={styles.value}>{account.firstName}</dd>
                    </div>
                )}
                <div className={styles.field}>
                    <dt className={styles.label}>
                        {I18n.t('confirmExternalStepup.familyName')}
                    </dt>
                    <dd className={styles.value}>{account.legalLastName}</dd>
                </div>
                {account.dateOfBirth != null && (
                    <div className={styles.field}>
                        <dt className={styles.label}>
                            {I18n.t('confirmExternalStepup.dateOfBirth')}
                        </dt>
                        <dd className={styles.value}>
                            {formatDateOfBirth(account.dateOfBirth)}
                        </dd>
                    </div>
                )}
            </dl>
            <Button
                type="button"
                onClick={() => {
                    window.location.href = proceedUrl;
                }}
                autoFocus
            >
                {I18n.t('confirmStepup.submit', {
                    serviceName: service.name,
                })}
            </Button>
        </LoginContainer>
    );
}
