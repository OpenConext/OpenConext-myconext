import { useState } from 'react';
import { flushSync } from 'react-dom';

import { useCopyToClipboard, useWarnBeforeUnload } from '@myconext/shared-gui';
import { Button } from '@surfnet/curve-react';

import I18n from '../../locale/I18n';
import { ErrorAlert } from '../ErrorAlert/ErrorAlert';
import { LoadingButton } from '../LoadingButton/LoadingButton';
import { LoginContainer } from '../LoginContainer/LoginContainer';
import styles from './AppEnrollment.module.scss';

interface RecoveryCodeViewProps {
    recoveryCode: string;
    proceedFailed: boolean;
    // returns false when the page is not left
    onNext: () => boolean;
}

export const RecoveryCodeView = ({
    recoveryCode,
    proceedFailed,
    onNext,
}: RecoveryCodeViewProps) => {
    const [leaving, setLeaving] = useState(false);
    const { copied, copy } = useCopyToClipboard(recoveryCode);
    useWarnBeforeUnload(!leaving);

    const next = () => {
        // The warning must be gone before the browser starts navigating away
        flushSync(() => setLeaving(true));
        if (!onNext()) {
            // Staying on the page, so the warning applies again
            setLeaving(false);
        }
    };

    return (
        <LoginContainer
            title={I18n.t('recovery.backupTitle')}
            description={I18n.t('recovery.backupDescription')}
            headerAlign="start"
        >
            <div className={styles.content}>
                <p className={styles.recoveryCode}>{recoveryCode}</p>
                <Button type="button" onClick={() => void copy()}>
                    {I18n.t(copied ? 'recovery.copied' : 'recovery.copy')}
                </Button>
                {proceedFailed && (
                    <ErrorAlert>{I18n.t('recovery.error')}</ErrorAlert>
                )}
                <LoadingButton
                    type="button"
                    variant="outline"
                    loading={leaving}
                    onClick={next}
                >
                    {I18n.t('recovery.next')}
                </LoadingButton>
            </div>
        </LoginContainer>
    );
};
