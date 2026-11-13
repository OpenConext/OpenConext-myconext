import { Navigate, useSearchParams } from 'react-router';

import { Button } from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import { configQueryOptions, rememberMe } from '../../api';
import {
    ErrorAlert,
    LoadingButton,
    LoadingLoginContainer,
    LoginContainer,
} from '../../components';
import I18n from '../../locale/I18n.ts';
import { getProceedUrl } from '../../utils/sso.ts';
import styles from './Remember.module.scss';

export default function Remember() {
    const [searchParams] = useSearchParams();

    const { data: config, isPending } = useQuery(configQueryOptions);

    const {
        mutate: remember,
        isPending: isRemembering,
        isSuccess,
        isError,
    } = useMutation<void, Response, string>({
        mutationFn: rememberMe,
    });

    if (isPending) {
        return <LoadingLoginContainer title={I18n.t('remember.title')} />;
    }

    const hash = searchParams.get('h');
    const proceedUrl = getProceedUrl(searchParams, config?.magicLinkUrl ?? '');
    if (!hash || !proceedUrl) {
        return <Navigate to="/expired" replace />;
    }

    const proceed = () => {
        window.location.href = proceedUrl;
    };

    return (
        <LoginContainer title={I18n.t('remember.title')} headerAlign="center">
            <p className={styles.info}>{I18n.t('remember.info')}</p>
            {isError && <ErrorAlert>{I18n.t('remember.error')}</ErrorAlert>}
            <div className={styles.actions}>
                <Button
                    type="button"
                    variant="outline"
                    className={styles.secondary}
                    disabled={isRemembering || isSuccess}
                    onClick={proceed}
                >
                    {I18n.t('remember.no')}
                </Button>
                <LoadingButton
                    type="button"
                    className={styles.primary}
                    loading={isRemembering}
                    disabled={isSuccess}
                    onClick={() => remember(hash, { onSuccess: proceed })}
                >
                    {I18n.t('remember.yes')}
                </LoadingButton>
            </div>
        </LoginContainer>
    );
}
