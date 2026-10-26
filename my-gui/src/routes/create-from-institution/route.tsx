import { useEffect } from 'react';
import { useSearchParams } from 'react-router';

import { BuildingOfficeIcon } from '@phosphor-icons/react';
import {
    Button,
    Card,
    CardContent,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
    Spinner,
} from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { startCreateFromInstitutionFlow } from '../../api';
import { GuestLayout } from '../../components/GuestLayout';
import I18n from '../../locale/I18n';
import { useConfigStore } from '../../store/config';
import './route.scss';

// public: a service provider sends the user here to create an eduID
export default function CreateFromInstitution() {
    const [searchParams] = useSearchParams();
    const returnTo = searchParams.get('return_to') ?? undefined;
    const config = useConfigStore((state) => state.config);
    const enabled = config?.createEduIDInstitutionEnabled !== false;
    const landing = Boolean(config?.createEduIDInstitutionLanding);

    // without a landing page the flow starts at once, otherwise on the button
    const flow = useQuery({
        queryKey: ['createFromInstitution', returnTo],
        queryFn: () => startCreateFromInstitutionFlow(false, returnTo),
        enabled: Boolean(config) && enabled,
        staleTime: Infinity,
        gcTime: 0,
        retry: false,
        refetchOnWindowFocus: false,
    });
    const url = flow.data?.url;

    useEffect(() => {
        if (url && config && !landing) {
            window.location.assign(url);
        }
    }, [url, config, landing]);

    return (
        <GuestLayout>
            {config && !enabled && (
                <p>{I18n.t('createFromInstitution.disabled')}</p>
            )}
            {config && enabled && landing && (
                <Card className="create-from-institution">
                    <CardHeader>
                        <CardTitle>
                            <BuildingOfficeIcon />
                            {I18n.t('createFromInstitution.title')}
                        </CardTitle>
                        <CardDescription>
                            {I18n.t('createFromInstitution.header')}
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        <p>{I18n.t('createFromInstitution.info')}</p>
                    </CardContent>
                    <CardFooter className="create-from-institution__footer">
                        <Button
                            data-testid="create-from-institution-start"
                            disabled={!url}
                            onClick={() => url && window.location.assign(url)}
                        >
                            {!url && <Spinner />}
                            {I18n.t('createFromInstitution.startFlow')}
                        </Button>
                        <p>
                            {I18n.t('createFromInstitution.alreadyHave')}{' '}
                            <a href={config.myconextBaseUrl}>
                                {I18n.t('createFromInstitution.login')}
                            </a>
                        </p>
                    </CardFooter>
                </Card>
            )}
            {(!config || (enabled && !landing)) && <Spinner />}
        </GuestLayout>
    );
}
