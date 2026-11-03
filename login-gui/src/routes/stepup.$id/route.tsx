import { Navigate, useParams, useSearchParams } from 'react-router';

import { BuildingOfficeIcon } from '@phosphor-icons/react';
import { Alert, AlertDescription, Button } from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { fetchServiceName } from '../../api';
import { LoginContainer } from '../../components';
import I18n from '../../locale/I18n.ts';

const LINKED_INSTITUTION = 'linked_institution';

export default function Stepup() {
    const { id = '' } = useParams();
    const [searchParams] = useSearchParams();
    
    const isSupported = searchParams.get('explanation') === LINKED_INSTITUTION;

    const {
        data: service,
        isPending,
        isError,
    } = useQuery({
        queryKey: ['serviceName', id],
        queryFn: () => fetchServiceName(id),
        enabled: !!id && isSupported,
    });

    if (!isSupported) {
        return null;
    }

    if (isError) {
        return <Navigate to="/expired" replace />;
    }

    if (isPending) {
        return null;
    }

    const serviceName = service.name;

    return (
        <LoginContainer
            title={I18n.t('stepup.title')}
            description={I18n.t('stepup.description', { serviceName })}
            headerAlign="start"
            footer={<p>{I18n.t('stepup.footer', { serviceName })}</p>}
        >
            <Alert>
                <BuildingOfficeIcon aria-hidden />
                <AlertDescription>
                    {I18n.t('stepup.linkedInstitution')}
                </AlertDescription>
            </Alert>
            <Button
                type="button"
                onClick={() => {
                    window.location.href = `/myconext/api/idp/oidc/account/${encodeURIComponent(id)}`;
                }}
                autoFocus
            >
                {I18n.t('stepup.submit')}
            </Button>
        </LoginContainer>
    );
}
