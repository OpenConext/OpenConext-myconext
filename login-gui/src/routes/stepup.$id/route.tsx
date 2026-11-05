import { useState } from 'react';
import { Navigate, useParams, useSearchParams } from 'react-router';

import { useQuery } from '@tanstack/react-query';

import { fetchServiceName } from '../../api';
import { Loading } from '../../components/Loading/Loading';
import I18n from '../../locale/I18n.ts';
import { StepupExplanation } from '../../components/Stepup/StepupExplanation';
import {
    MODE_BY_EXPLANATION,
    isSupportedExplanation,
} from '../../components/Stepup/ValidateNamesMode';
import { ValidateNamesView } from '../../components/Stepup/ValidateNamesView';

export default function Stepup() {
    const { id = '' } = useParams();
    const [searchParams] = useSearchParams();

    const [showValidateExplanation, setShowValidateExplanation] =
        useState(true);

    const explanationParam = searchParams.get('explanation');
    const isSupported = isSupportedExplanation(explanationParam);

    const {
        data: service,
        isPending,
        isError,
    } = useQuery({
        queryKey: ['serviceName', id],
        queryFn: () => fetchServiceName(id),
        enabled: !!id && isSupported,
    });

    if (isError || !isSupportedExplanation(explanationParam)) {
        return <Navigate to="/expired" replace />;
    }

    if (isPending) {
        return <Loading title={I18n.t('stepup.title')} />;
    }

    if (showValidateExplanation) {
        return (
            <StepupExplanation
                explanation={explanationParam}
                serviceName={service.name}
                onContinue={() => setShowValidateExplanation(false)}
            />
        );
    }

    return (
        <ValidateNamesView
            mode={MODE_BY_EXPLANATION[explanationParam]}
            onUseInstitution={() => {
                window.location.href = `/myconext/api/idp/oidc/account/${encodeURIComponent(id)}`;
            }}
        />
    );
}
