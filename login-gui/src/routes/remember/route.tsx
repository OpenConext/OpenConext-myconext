import { Navigate, useSearchParams } from 'react-router';

import { Button } from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import { fetchConfig, rememberMe } from '../../api';
import { LoginContainer } from '../../components';
import I18n from '../../locale/I18n.ts';
import { getProceedUrl } from '../../utils/sso.ts';
import styles from './Remember.module.scss';

export default function Remember() {
    const [searchParams] = useSearchParams();

    const { data: config, isPending } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });

    const {
        mutate: remember,
        isPending: isRemembering,
        isSuccess,
        isError,
    } = useMutation<void, Response, string>({
        mutationFn: rememberMe,
    });

    if (isPending) {
        return null;
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
        <LoginContainer
            title={I18n.t('remember.title')}
            description={I18n.t('remember.info')}
            headerAlign="start"
        >
            {isError && <p role="alert">{I18n.t('remember.error')}</p>}
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
                <Button
                    type="button"
                    className={styles.primary}
                    disabled={isRemembering || isSuccess}
                    onClick={() => remember(hash, { onSuccess: proceed })}
                >
                    {I18n.t('remember.yes')}
                </Button>
            </div>
        </LoginContainer>
    );
}
