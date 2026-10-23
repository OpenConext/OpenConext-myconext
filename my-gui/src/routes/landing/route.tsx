import { useNavigate, useSearchParams } from 'react-router';

import { SignOutIcon, TrashIcon, WarningIcon } from '@phosphor-icons/react';
import {
    Alert,
    AlertAction,
    AlertDescription,
    AlertTitle,
    Button,
    Empty,
    EmptyContent,
    EmptyDescription,
    EmptyHeader,
    EmptyMedia,
    EmptyTitle,
} from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { fetchConfig, redirectToLogin } from '../../api';
import { GuestLayout } from '../../components/GuestLayout';
import I18n from '../../locale/I18n';
import './route.scss';

const REASONS = {
    logout: { icon: <SignOutIcon />, action: 'loginAgain' },
    delete: { icon: <TrashIcon />, action: 'registerAgain' },
} as const;

// public, the server sends the browser here after the logout of the account
function LandingContent() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { data: config } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });

    const reason = (['logout', 'delete', 'ratelimit'] as const).find((key) =>
        searchParams.get(key),
    );

    if (!reason) {
        return (
            <div className="landing">
                <Empty>
                    <EmptyHeader>
                        <EmptyTitle>{I18n.t('landing.none')}</EmptyTitle>
                    </EmptyHeader>
                    <EmptyContent>
                        <Button onClick={() => void navigate('/')}>
                            {I18n.t('landing.loginAgain')}
                        </Button>
                    </EmptyContent>
                </Empty>
            </div>
        );
    }

    if (reason === 'ratelimit') {
        return (
            <div className="landing">
                <Alert variant="warning" className="landing__alert">
                    <WarningIcon />
                    <AlertTitle>{I18n.t('landing.ratelimitTitle')}</AlertTitle>
                    <AlertDescription>
                        {I18n.t('landing.ratelimitStatus')}
                    </AlertDescription>
                    <AlertAction>
                        <Button
                            disabled={!config?.loginUrl}
                            onClick={() =>
                                config?.loginUrl &&
                                redirectToLogin(
                                    config.loginUrl,
                                    undefined,
                                    false,
                                )
                            }
                        >
                            {I18n.t('landing.loginAgain')}
                        </Button>
                    </AlertAction>
                </Alert>
            </div>
        );
    }

    const { icon, action } = REASONS[reason];

    return (
        <div className="landing">
            <Empty>
                <EmptyHeader>
                    <EmptyMedia variant="icon">{icon}</EmptyMedia>
                    <EmptyTitle>{I18n.t(`landing.${reason}Title`)}</EmptyTitle>
                    <EmptyDescription>
                        {I18n.t(
                            `landing.${reason === 'delete' ? 'delete' : 'logout'}Status`,
                        )}
                    </EmptyDescription>
                </EmptyHeader>
                <EmptyContent>
                    <Button
                        disabled={!config?.loginUrl}
                        onClick={() =>
                            config?.loginUrl &&
                            redirectToLogin(config.loginUrl, undefined, true)
                        }
                    >
                        {I18n.t(`landing.${action}`)}
                    </Button>
                </EmptyContent>
            </Empty>
        </div>
    );
}

export default function Landing() {
    return (
        <GuestLayout>
            <LandingContent />
        </GuestLayout>
    );
}
