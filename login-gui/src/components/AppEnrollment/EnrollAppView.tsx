import { isTouchDevice } from '@myconext/shared-gui';
import { Button, Spinner } from '@surfnet/curve-react';

import type { StartEnrollmentResponse } from '../../api';
import I18n from '../../locale/I18n';
import { LoginContainer } from '../LoginContainer/LoginContainer';
import styles from './AppEnrollment.module.scss';

export type EnrollAppState =
    | { phase: 'scan'; start: StartEnrollmentResponse }
    | { phase: 'finish' }
    | { phase: 'timedOut' }
    | { phase: 'existing' }
    | { phase: 'error' };

interface EnrollAppViewProps {
    state: EnrollAppState;
    onResume: () => void;
}

const reload = () => window.location.reload();

export const EnrollAppView = ({ state, onResume }: EnrollAppViewProps) => {
    switch (state.phase) {
        case 'scan': {
            const qr = (
                <img
                    className={styles.qr}
                    src={state.start.qrcode}
                    alt={I18n.t('enrollApp.qrAlt')}
                />
            );

            return (
                <LoginContainer
                    title={I18n.t('enrollApp.scanTitle')}
                    description={I18n.t('enrollApp.scanDescription')}
                    headerAlign="start"
                >
                    {isTouchDevice() ? (
                        <div className={styles.center}>
                            <a href={state.start.url}>{qr}</a>
                            <Button
                                type="button"
                                onClick={() =>
                                    (window.location.href = state.start.url)
                                }
                            >
                                {I18n.t('enrollApp.openApp')}
                            </Button>
                        </div>
                    ) : (
                        <div className={styles.center}>{qr}</div>
                    )}
                </LoginContainer>
            );
        }
        case 'finish':
            return (
                <LoginContainer
                    title={I18n.t('enrollApp.finishTitle')}
                    description={I18n.t('enrollApp.finishDescription')}
                    headerAlign="start"
                >
                    <div className={styles.center}>
                        <Spinner />
                    </div>
                </LoginContainer>
            );
        case 'timedOut':
            return (
                <LoginContainer
                    title={I18n.t('enrollApp.timeOutTitle')}
                    description={I18n.t('enrollApp.timeOutDescription')}
                    headerAlign="start"
                >
                    <Button type="button" onClick={reload}>
                        {I18n.t('enrollApp.retry')}
                    </Button>
                </LoginContainer>
            );
        case 'existing':
            return (
                <LoginContainer
                    title={I18n.t('enrollApp.existingTitle')}
                    description={I18n.t('enrollApp.existingDescription')}
                    headerAlign="start"
                >
                    <Button type="button" onClick={onResume} autoFocus>
                        {I18n.t('enrollApp.existingResume')}
                    </Button>
                </LoginContainer>
            );
        case 'error':
            return (
                <LoginContainer
                    title={I18n.t('enrollApp.scanTitle')}
                    description={I18n.t('enrollApp.error')}
                    headerAlign="start"
                >
                    <Button type="button" onClick={reload}>
                        {I18n.t('enrollApp.retry')}
                    </Button>
                </LoginContainer>
            );
    }
};
