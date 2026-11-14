import { Navigate, useNavigate, useSearchParams } from 'react-router';

import { useQuery } from '@tanstack/react-query';

import { configQueryOptions } from '../../api';
import { LoadingLoginContainer, NudgeAppView } from '../../components';
import I18n from '../../locale/I18n';
import { getProceedUrl } from '../../utils/sso';

export default function Confirm() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const hash = searchParams.get('h') ?? '';

    const { data: config, isPending } = useQuery(configQueryOptions);

    if (!hash) {
        return <Navigate to="/expired" replace />;
    }

    if (isPending) {
        return <LoadingLoginContainer title={I18n.t('nudgeApp.title')} />;
    }

    const proceedUrl = getProceedUrl(searchParams, config?.magicLinkUrl ?? '');
    if (!proceedUrl) {
        return <Navigate to="/expired" replace />;
    }

    return (
        <NudgeAppView
            onDownload={() => navigate(`/getapp?h=${encodeURIComponent(hash)}`)}
            onSkip={() => {
                window.location.href = proceedUrl;
            }}
        />
    );
}
