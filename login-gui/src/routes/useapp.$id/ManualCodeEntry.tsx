import { useState } from 'react';

import { OtpInput } from '@myconext/shared-gui';
import { WarningDiamondIcon } from '@phosphor-icons/react';
import { Alert, AlertDescription, Button } from '@surfnet/curve-react';
import { useMutation } from '@tanstack/react-query';

import { tiqrManualResponse } from '../../api';
import I18n from '../../locale/I18n';
import styles from './route.module.scss';

type ManualCodeEntryProps = {
    sessionKey: string;
};

export const ManualCodeEntry = ({ sessionKey }: ManualCodeEntryProps) => {
    const [codeRequested, setCodeRequested] = useState(false);
    const [code, setCode] = useState('');

    const {
        mutate: submitCode,
        isPending: isVerifying,
        isError: codeFailed,
    } = useMutation({
        mutationFn: (value: string) => tiqrManualResponse(sessionKey, value),
        // On success the next poll returns SUCCESS and handles the redirect
        onError: () => setCode(''),
    });

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
                        ariaLabel={I18n.t('useApp.codeLabel')}
                        value={code}
                        onChange={setCode}
                        disabled={isVerifying}
                        onComplete={(value) => submitCode(value.trim())}
                    />
                    {codeFailed && (
                        <Alert variant="warning">
                            <WarningDiamondIcon />
                            <AlertDescription>
                                {I18n.t('useApp.codeIncorrect')}
                            </AlertDescription>
                        </Alert>
                    )}
                </div>
            )}
        </>
    );
};
