import { type ReactNode, useState } from 'react';

import type { TiqrFlow } from '../../hooks/useTiqrAuthentication';
import I18n from '../../locale/I18n';
import type { ManualCode } from './ManualCodeEntry';
import { PushPrompt } from './PushPrompt';
import { QrScan } from './QrScan';
import { StartError } from './StartError';
import { SuspendedAlert } from './SuspendedAlert';
import { TimedOut } from './TimedOut';

export type AppFlowScreen = {
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
): AppFlowScreen => {
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

export const useAppFlowScreen = (
    flow: TiqrFlow,
    manualCode: ManualCode,
): AppFlowScreen => {
    const [qrRequested, setQrRequested] = useState(false);

    return getScreen(flow, manualCode, qrRequested, () => setQrRequested(true));
};
