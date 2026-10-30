import { type ReactNode, useState } from 'react';
import { Navigate, useLocation, useNavigate, useParams } from 'react-router';

import { LoginContainer } from '../../components';
import I18n from '../../locale/I18n';
import { PushPrompt } from './PushPrompt';
import { QrScan } from './QrScan';
import { StartError } from './StartError';
import { SuspendedAlert } from './SuspendedAlert';
import { TimedOut } from './TimedOut';
import { UseAppFooter } from './UseAppFooter';
import { type TiqrFlow, useTiqrAuthentication } from './useTiqrAuthentication';

type Screen = {
    title: string;
    description?: string;
    showFooter: boolean;
    body: ReactNode;
};

const getScreen = (
    flow: TiqrFlow,
    qrRequested: boolean,
    onShowQr: () => void,
): Screen => {
    switch (flow.phase) {
        case 'timedOut':
            return {
                title: I18n.t('useApp.timeOutTitle'),
                showFooter: true,
                body: <TimedOut />,
            };
        case 'startError':
            return {
                title: I18n.t('useApp.title'),
                showFooter: true,
                body: <StartError />,
            };
        case 'loading':
            return {
                title: I18n.t('useApp.title'),
                showFooter: false,
                body: I18n.t('useApp.loading'),
            };
        case 'active': {
            const suspendedAlert = (
                <SuspendedAlert minutes={flow.suspendedMinutes} />
            );

            if (qrRequested || !flow.start.tiqrCookiePresent) {
                return {
                    title: I18n.t('useApp.scanTitle'),
                    showFooter: true,
                    body: (
                        <>
                            <QrScan start={flow.start} />
                            {suspendedAlert}
                        </>
                    ),
                };
            }

            return {
                title: I18n.t('useApp.title'),
                description: I18n.t('useApp.description'),
                showFooter: true,
                body: (
                    <>
                        <PushPrompt onShowQr={onShowQr} />
                        {suspendedAlert}
                    </>
                ),
            };
        }
    }
};

type UseAppFlowProps = {
    email: string;
    id: string;
};

const UseAppFlow = ({ email, id }: UseAppFlowProps) => {
    const navigate = useNavigate();
    const flow = useTiqrAuthentication(email, id);
    const [qrRequested, setQrRequested] = useState(false);

    const goToOptions = () => navigate(`/options/${id}`, { state: { email } });

    const { title, description, showFooter, body } = getScreen(
        flow,
        qrRequested,
        () => setQrRequested(true),
    );

    return (
        <LoginContainer
            title={title}
            description={description}
            footer={
                showFooter ? (
                    <UseAppFooter onOtherMethod={goToOptions} />
                ) : undefined
            }
            onBack={goToOptions}
            backLabel={email}
        >
            {body}
        </LoginContainer>
    );
};

export default function UseApp() {
    const { id = '' } = useParams();
    const { state } = useLocation();
    const email = typeof state?.email === 'string' ? state.email : '';

    if (!email) {
        return <Navigate to={`/login/${id}`} replace />;
    }

    return <UseAppFlow email={email} id={id} />;
}
