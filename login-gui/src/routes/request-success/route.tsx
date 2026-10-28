import { Navigate, useSearchParams } from 'react-router';

import { Button } from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { fetchConfig } from '../../api';
import { LoginContainer } from '../../components';
import I18n from '../../locale/I18n.ts';
import { getProceedUrl } from '../../utils/sso.ts';

export default function RequestSuccess() {
    const [searchParams] = useSearchParams();
    const { data: config, isPending } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });

    if (isPending) {
        return null;
    }

    const proceedUrl = getProceedUrl(searchParams, config?.magicLinkUrl ?? '');
    if (!proceedUrl) {
        return <Navigate to="/expired" replace />;
    }

    return (
        <LoginContainer title={I18n.t('requestSuccess.title')}>
            <p>{I18n.t('requestSuccess.info')}</p>
            <Button
                type="button"
                onClick={() => {
                    window.location.href = proceedUrl;
                }}
            >
                {I18n.t('requestSuccess.next')}
            </Button>
        </LoginContainer>
    );
}
