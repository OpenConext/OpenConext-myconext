import { useState } from 'react';

import { ArrowCounterClockwiseIcon } from '@phosphor-icons/react';
import { Button } from '@surfnet/curve-react';

import { PageHeader } from '../components/PageHeader';
import I18n from '../locale/I18n';
import { useAppStore } from '../stores/AppStore';
import Confirmation from '../tabs/Confirmation';
import Control from '../tabs/Control';
import Verification from '../tabs/Verification';
import './IdentityCheck.scss';

const IdentityCheck = () => {
    const [step, setStep] = useState(1);
    const code = useAppStore((state) => state.controlCode.code);

    const restart = () => setStep(1);

    return (
        <main
            className={`page-main identity-check ${step === 3 ? 'identity-check--done' : ''}`}
        >
            {step !== 3 && (
                <PageHeader
                    subtitle={
                        step === 2 && (
                            <>
                                {I18n.t('control.forCode')}{' '}
                                <strong>{code}</strong>
                            </>
                        )
                    }
                    action={
                        step === 2 && (
                            <Button variant="outline" onClick={restart}>
                                <ArrowCounterClockwiseIcon />
                                {I18n.t('control.restart')}
                            </Button>
                        )
                    }
                >
                    {I18n.t('identityCheck.title')}
                </PageHeader>
            )}
            <div className="identity-check__content">
                {step === 1 && <Verification proceed={() => setStep(2)} />}
                {step === 2 && <Control proceed={() => setStep(3)} />}
                {step === 3 && <Confirmation restart={restart} />}
            </div>
        </main>
    );
};

export default IdentityCheck;
