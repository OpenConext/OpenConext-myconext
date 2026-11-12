import type { Ref, SubmitEvent } from 'react';

import { OtpInput } from '@myconext/shared-gui';
import { Button } from '@surfnet/curve-react';

import type { SendError } from '../../api';
import I18n from '../../locale/I18n';
import { ErrorAlert } from '../ErrorAlert/ErrorAlert';
import { LoadingButton } from '../LoadingButton/LoadingButton';
import { LoginContainer } from '../LoginContainer/LoginContainer';
import styles from './PhoneRecovery.module.scss';

export type CodeError = 'incorrect' | 'maxAttempts' | 'failed';

interface PhoneConfirmationViewProps {
    phoneNumber: string;
    code: string;
    inputRef: Ref<HTMLInputElement>;
    onCodeChange: (value: string) => void;
    onComplete: (value: string) => void;
    onSubmit: (event: SubmitEvent<HTMLFormElement>) => void;
    onResend: () => void;
    onChangeNumber: () => void;
    // false while the verification can not be completed yet
    ready: boolean;
    isVerifying: boolean;
    isResending: boolean;
    resent: boolean;
    codeError: CodeError | null;
    resendError: SendError | null;
}

export const PhoneConfirmationView = ({
    phoneNumber,
    code,
    inputRef,
    onCodeChange,
    onComplete,
    onSubmit,
    onResend,
    onChangeNumber,
    ready,
    isVerifying,
    isResending,
    resent,
    codeError,
    resendError,
}: PhoneConfirmationViewProps) => (
    <LoginContainer
        title={I18n.t('phoneConfirmation.title')}
        description={I18n.t('phoneConfirmation.description', { phoneNumber })}
        headerAlign="start"
        onBack={onChangeNumber}
        backLabel={phoneNumber}
        footer={
            <>
                <p>{I18n.t('phoneConfirmation.noCode')}</p>
                <LoadingButton
                    type="button"
                    variant="link"
                    className={styles.link}
                    loading={isResending}
                    onClick={onResend}
                >
                    {I18n.t('phoneConfirmation.resend')}
                </LoadingButton>
            </>
        }
    >
        <form className={styles.form} onSubmit={onSubmit}>
            <OtpInput
                ref={inputRef}
                ariaLabel={I18n.t('phoneConfirmation.inputLabel')}
                value={code}
                onChange={onCodeChange}
                disabled={!ready || isVerifying || codeError === 'maxAttempts'}
                onComplete={onComplete}
            />
            {codeError === 'maxAttempts' ? (
                <ErrorAlert>
                    {I18n.t('phoneConfirmation.maxAttempts')}{' '}
                    <Button
                        type="button"
                        variant="link"
                        className={styles.link}
                        onClick={onChangeNumber}
                    >
                        {I18n.t('phoneConfirmation.changeNumber')}
                    </Button>
                </ErrorAlert>
            ) : (
                codeError !== null && (
                    <ErrorAlert>
                        {I18n.t(
                            codeError === 'incorrect'
                                ? 'phoneConfirmation.incorrect'
                                : 'phoneConfirmation.error',
                        )}
                    </ErrorAlert>
                )
            )}
            {resendError !== null && (
                <ErrorAlert>
                    {I18n.t(
                        resendError === 'rateLimited'
                            ? 'phoneVerification.rateLimited'
                            : 'phoneVerification.error',
                    )}
                </ErrorAlert>
            )}
            {resent && resendError === null && (
                <p role="status" className={styles.status}>
                    {I18n.t('phoneConfirmation.resent')}
                </p>
            )}
            <LoadingButton
                type="submit"
                loading={isVerifying}
                disabled={
                    !ready || code.length === 0 || codeError === 'maxAttempts'
                }
            >
                {I18n.t('phoneConfirmation.submit')}
            </LoadingButton>
        </form>
    </LoginContainer>
);
