import { useState } from 'react';

import { OtpInput } from '@myconext/shared-gui';
import { WarningCircleIcon } from '@phosphor-icons/react';
import { Button } from '@surfnet/curve-react';

import { getUserControlCode } from '../api';
import I18n from '../locale/I18n';
import { useAppStore } from '../stores/AppStore';
import './Verification.scss';

const CODE_LENGTH = 6;

const Verification = ({ proceed }: { proceed: () => void }) => {
    const [error, setError] = useState(false);
    const [code, setCode] = useState('');
    const [lastCode, setLastCode] = useState('');
    const setFlash = useAppStore((state) => state.setFlash);

    const doGetUserControlCode = async (val: string) => {
        setLastCode(val);
        try {
            const controlCode = await getUserControlCode(val);
            setFlash(
                I18n.t('verification.flash', {
                    name: `${controlCode.firstName} ${controlCode.lastName}`,
                }),
            );
            useAppStore.setState(() => ({ controlCode }));
            setCode('');
            setError(false);
            proceed();
        } catch {
            setCode('');
            setError(true);
        }
    };

    return (
        <section className="verification">
            <div className="verification__intro">
                <h2>{I18n.t('identityCheck.codeTitle')}</h2>
                <p>{I18n.t('verification.info')}</p>
            </div>
            <OtpInput
                length={CODE_LENGTH}
                ariaLabel={I18n.t('identityCheck.codeTitle')}
                value={code}
                onChange={(val) => {
                    setCode(val);
                    setError(false);
                }}
                onComplete={(val) => void doGetUserControlCode(val)}
            />
            {error && (
                <div className="error-indicator" role="alert">
                    <WarningCircleIcon weight="fill" />
                    <span>
                        {I18n.t('verification.error', { code: lastCode })}
                    </span>
                </div>
            )}
            <Button
                disabled={code.length !== CODE_LENGTH}
                onClick={() => void doGetUserControlCode(code)}
            >
                {I18n.t('verification.proceed')}
            </Button>
        </section>
    );
};

export default Verification;
