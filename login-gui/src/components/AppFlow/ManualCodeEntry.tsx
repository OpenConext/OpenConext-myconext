import { useEffect, useRef, useState } from 'react';

import { OtpInput } from '@myconext/shared-gui';
import { Button } from '@surfnet/curve-react';

import I18n from '../../locale/I18n';
import { ErrorAlert } from '../ErrorAlert/ErrorAlert';
import { Spinner } from '../Loading/Spinner';
import styles from './AppFlow.module.scss';

export type ManualCode = {
    submit: (code: string) => Promise<boolean>;
    isVerifying: boolean;
    failed: boolean;
};

type ManualCodeEntryProps = {
    manualCode: ManualCode;
};

export const ManualCodeEntry = ({ manualCode }: ManualCodeEntryProps) => {
    const [codeRequested, setCodeRequested] = useState(false);
    const [code, setCode] = useState('');
    const otpInputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        if (!manualCode.isVerifying) {
            otpInputRef.current?.focus();
        }
    }, [manualCode.isVerifying]);

    const submitCode = async (value: string) => {
        if (!(await manualCode.submit(value))) {
            setCode('');
        }
    };

    return (
        <>
            <div className={styles.help}>
                <span>{I18n.t('useApp.offline')}</span>
                <Button
                    variant="link"
                    type="button"
                    className={styles.link}
                    onClick={() => setCodeRequested(true)}
                >
                    {I18n.t('useApp.enterCode')}
                </Button>
            </div>

            {codeRequested && (
                <div className={styles.code}>
                    <OtpInput
                        ref={otpInputRef}
                        ariaLabel={I18n.t('useApp.codeLabel')}
                        value={code}
                        onChange={setCode}
                        disabled={manualCode.isVerifying}
                        onComplete={(value) => void submitCode(value.trim())}
                    />
                    {manualCode.isVerifying && <Spinner />}
                    {manualCode.failed && (
                        <ErrorAlert>
                            {I18n.t('useApp.codeIncorrect')}
                        </ErrorAlert>
                    )}
                </div>
            )}
        </>
    );
};
