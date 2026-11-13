import { Navigate, useSearchParams } from 'react-router';

import { CheckCircleIcon } from '@phosphor-icons/react';
import { Alert, AlertDescription, Button } from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { configQueryOptions, fetchServiceNameByHash } from '../../api';
import { LoadingLoginContainer, LoginContainer } from '../../components';
import I18n from '../../locale/I18n.ts';
import { getProceedUrl } from '../../utils/sso.ts';

const EXPLANATIONS = [
    'linked_institution',
    'validate_names',
    'validate_names_external',
    'affiliation_student',
    'profile_mfa',
    'any_link',
] as const;

type Explanation = (typeof EXPLANATIONS)[number];

const isExplanation = (value: string | null): value is Explanation =>
    EXPLANATIONS.some((explanation) => explanation === value);

export default function ConfirmStepup() {
    const [searchParams] = useSearchParams();
    const hash = searchParams.get('h') ?? '';
    const explanationParam = searchParams.get('explanation');
    const explanation: Explanation = isExplanation(explanationParam)
        ? explanationParam
        : 'any_link';

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
        return <LoadingLoginContainer title={I18n.t('confirmStepup.title')} />;
    }

    const proceedUrl = getProceedUrl(searchParams, config?.magicLinkUrl ?? '');
    if (!proceedUrl) {
        return <Navigate to="/expired" replace />;
    }

    const serviceName = service.name;

    return (
        <LoginContainer
            title={I18n.t('confirmStepup.title')}
            description={I18n.t('confirmStepup.conditionMet')}
            headerAlign="start"
        >
            <Alert>
                <CheckCircleIcon aria-hidden />
                <AlertDescription>
                    {I18n.t(`confirmStepup.explanation.${explanation}`)}
                </AlertDescription>
            </Alert>
            <Button
                type="button"
                onClick={() => {
                    window.location.href = proceedUrl;
                }}
                autoFocus
            >
                {I18n.t('confirmStepup.submit', { serviceName })}
            </Button>
        </LoginContainer>
    );
}
