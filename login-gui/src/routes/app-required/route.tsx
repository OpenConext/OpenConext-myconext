import { Navigate, useNavigate, useSearchParams } from 'react-router';

import { useQuery } from '@tanstack/react-query';

import { configQueryOptions, fetchServiceNameByHash } from '../../api';
import { AppRequiredView } from '../../components';
import { getProceedUrl } from '../../utils/sso';

export default function AppRequired() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const hash = searchParams.get('h') ?? '';

    const { data: config, isPending: isConfigPending } =
        useQuery(configQueryOptions);

    const {
        data: service,
        isPending: isServicePending,
        isError,
    } = useQuery({
        queryKey: ['serviceNameByHash', hash],
        queryFn: () => fetchServiceNameByHash(hash),
        enabled: !!hash,
    });

    if (!hash || isError) {
        return <Navigate to="/expired" replace />;
    }

    if (isConfigPending || isServicePending) {
        return null;
    }

    const proceedUrl = getProceedUrl(searchParams, config?.magicLinkUrl ?? '');
    if (!proceedUrl) {
        return <Navigate to="/expired" replace />;
    }

    return (
        <AppRequiredView
            serviceName={service.name}
            onDownload={() => navigate(`/getapp?h=${encodeURIComponent(hash)}`)}
            onRefuse={() => {
                window.location.href = proceedUrl;
            }}
        />
    );
}
