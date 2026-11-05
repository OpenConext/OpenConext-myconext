import { useState } from 'react';
import { Navigate, useParams, useSearchParams } from 'react-router';

import { useQuery } from '@tanstack/react-query';

import { configQueryOptions, fetchServiceName } from '../../api';
import { Loading } from '../../components/Loading/Loading';
import { StepupExplanation } from '../../components/Stepup/StepupExplanation';
import {
    MODE_BY_EXPLANATION,
    ValidateNamesMode,
    isSupportedExplanation,
} from '../../components/Stepup/ValidateNamesMode';
import { ValidateNamesView } from '../../components/Stepup/ValidateNamesView';
import I18n from '../../locale/I18n.ts';

export default function Stepup() {
    const { id = '' } = useParams();
    const [searchParams] = useSearchParams();

    // After a failed iDIN/eIDAS round-trip the old app went straight to the
    // alternatives, skipping the explanation.
    const isRetry = searchParams.has('retry');
    const [showValidateExplanation, setShowValidateExplanation] =
        useState(!isRetry);

    const { data: config, isPending: configPending } =
        useQuery(configQueryOptions);

    const goToInstitution = () => {
        window.location.href = `/myconext/api/idp/oidc/account/${encodeURIComponent(id)}`;
    };

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

    if (isPending || configPending) {
        return <Loading title={I18n.t('stepup.title')} />;
    }

    if (showValidateExplanation) {
        return (
            <StepupExplanation
                explanation={explanationParam}
                serviceName={service.name}
                onContinue={() => {
                    if (!config?.featureIdVerify) {
                        goToInstitution();
                        return;
                    }
                    setShowValidateExplanation(false);
                }}
            />
        );
    }

    return (
        <ValidateNamesView
            id={id}
            mode={
                isRetry
                    ? ValidateNamesMode.AlternativesOnly
                    : MODE_BY_EXPLANATION[explanationParam]
            }
            onUseInstitution={goToInstitution}
        />
    );
}
