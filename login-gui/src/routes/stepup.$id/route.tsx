import { useState } from 'react';
import { Navigate, useParams, useSearchParams } from 'react-router';

import { BuildingOfficeIcon } from '@phosphor-icons/react';
import { Alert, AlertDescription, Button } from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { fetchServiceName } from '../../api';
import { LoginContainer } from '../../components';
import I18n from '../../locale/I18n.ts';
import { ValidateNamesView } from './ValidateNamesView';

const LINKED_INSTITUTION = 'linked_institution';
const VALIDATE_NAMES = 'validate_names';
const AFFILIATION_STUDENT = 'affiliation_student';

// Unused for now
const VALIDATE_NAMES_EXTERNAL = 'validate_names_external';
const PROFILE_MFA = 'profile_mfa';
const ANY_LINK = 'any_link';

const SUPPORTED_EXPLANATIONS = [
    VALIDATE_NAMES,
    LINKED_INSTITUTION,
    AFFILIATION_STUDENT,
] as const;

type SupportedExplanation = (typeof SUPPORTED_EXPLANATIONS)[number];

const isSupportedExplanation = (
    value: string | null,
): value is SupportedExplanation =>
    SUPPORTED_EXPLANATIONS.some((explanation) => explanation === value);

export default function Stepup() {
    const { id = '' } = useParams();
    const [searchParams] = useSearchParams();

    // Temporary switch until the validate_names flow is wired up.
    const [showValidateNames, setShowValidateNames] = useState(false);

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

    if (!isSupportedExplanation(explanationParam)) {
        return null;
    }

    if (isError) {
        return <Navigate to="/expired" replace />;
    }

    if (isPending) {
        return null;
    }

    const serviceName = service.name;

    if (showValidateNames) {
        return (
            <ValidateNamesView
                onUseInstitution={() => {
                    window.location.href = `/myconext/api/idp/oidc/account/${encodeURIComponent(id)}`;
                }}
            />
        );
    }

    return (
        <LoginContainer
            title={I18n.t('stepup.title')}
            description={I18n.t(`stepup.${explanationParam}.description`, {
                serviceName,
            })}
            headerAlign="start"
            footer={
                <p>
                    {I18n.t(`stepup.${explanationParam}.footer`, {
                        serviceName,
                    })}
                </p>
            }
        >
            <Alert>
                <BuildingOfficeIcon aria-hidden />
                <AlertDescription>
                    {I18n.t(`stepup.${explanationParam}.alert`)}
                </AlertDescription>
            </Alert>
            <Button
                type="button"
                onClick={() => setShowValidateNames(true)}
                autoFocus
            >
                {I18n.t('stepup.submit')}
            </Button>
        </LoginContainer>
    );
}
