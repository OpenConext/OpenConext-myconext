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
    const explanation = isSupportedExplanation(explanationParam)
        ? explanationParam
        : null;
    // A retry skips the explanation, so (e.g. from the *-already-linked pages) it needs no explanation param.
    const canRender = isRetry || explanation !== null;

    const {
        data: service,
        isPending,
        isError,
    } = useQuery({
        queryKey: ['serviceName', id],
        queryFn: () => fetchServiceName(id),
        enabled: !!id && canRender,
    });

    if (isError || !canRender) {
        return <Navigate to="/expired" replace />;
    }

    if (isPending || configPending) {
        return <Loading title={I18n.t('stepup.title')} />;
    }

    if (showValidateExplanation && explanation) {
        return (
            <StepupExplanation
                explanation={explanation}
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
                isRetry || !explanation
                    ? ValidateNamesMode.AlternativesOnly
                    : MODE_BY_EXPLANATION[explanation]
            }
            onUseInstitution={goToInstitution}
        />
    );
}
