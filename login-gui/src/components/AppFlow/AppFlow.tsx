import { type ReactNode, useState } from 'react';

import type { TiqrStartResponse } from '../../api';
import I18n from '../../locale/I18n';
import { LoginContainer } from '../LoginContainer/LoginContainer';
import type { ManualCode } from './ManualCodeEntry';
import { PushPrompt } from './PushPrompt';
import { QrScan } from './QrScan';
import { StartError } from './StartError';
import { SuspendedAlert } from './SuspendedAlert';
import { TimedOut } from './TimedOut';
import { UseAppFooter } from './UseAppFooter';

export type { ManualCode } from './ManualCodeEntry';

export type TiqrFlow =
    | { phase: 'loading' }
    | { phase: 'startError' }
    | { phase: 'timedOut' }
    | {
          phase: 'active';
          start: TiqrStartResponse;
          suspendedMinutes: number | null;
      };

type Screen = {
    title: string;
    description?: string;
    showFooter: boolean;
    body: ReactNode;
};

const getScreen = (
    flow: TiqrFlow,
    manualCode: ManualCode,
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
                            <QrScan
                                start={flow.start}
                                manualCode={manualCode}
                            />
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

type AppFlowProps = {
    email: string;
    flow: TiqrFlow;
    manualCode: ManualCode;
    onOtherMethod: () => void;
};

export const AppFlow = ({
    email,
    flow,
    manualCode,
    onOtherMethod,
}: AppFlowProps) => {
    const [qrRequested, setQrRequested] = useState(false);

    const { title, description, showFooter, body } = getScreen(
        flow,
        manualCode,
        qrRequested,
        () => setQrRequested(true),
    );

    return (
        <LoginContainer
            title={title}
            description={description}
            footer={
                showFooter ? (
                    <UseAppFooter onOtherMethod={onOtherMethod} />
                ) : undefined
            }
            onBack={onOtherMethod}
            backLabel={email}
        >
            {body}
        </LoginContainer>
    );
};
