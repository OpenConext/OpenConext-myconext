import { useRef, useState } from 'react';
import type { FormEvent } from 'react';

import {
    Button,
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    Field,
    FieldError,
    FieldGroup,
    FieldLabel,
    Input,
    InputOTP,
    InputOTPGroup,
    InputOTPSlot,
    Spinner,
} from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import {
    fetchConfig,
    finishEnrollment,
    generateBackupCode,
    sendPhoneCode,
    verifyPhoneCode,
} from '../api';
import I18n from '../locale/I18n';
import './RecoveryDialog.scss';

// the input is re-enabled after the failed request has settled
const FOCUS_DELAY = 50;

type Step = 'choose' | 'phone' | 'sms' | 'backup' | 'congrats';

const CODE_LENGTH = 6;
const COPIED_TIMEOUT = 1150;
const PHONE_NUMBER = /^\+?(?:[0-9] ?){6,14}[0-9]$/;

const sanitizePhoneNumber = (value: string) => value.replace(/[ -]/g, '');

// the server shows the recovery code in two parts
const formatRecoveryCode = (code: string) =>
    `${code.substring(0, 4)} ${code.substring(4)}`;

type RecoveryDialogProps = {
    onClose: () => void;
};

// the last step of enrolling the eduID app: a recovery method, then finishing
export const RecoveryDialog = ({ onClose }: RecoveryDialogProps) => {
    const otpRef = useRef<HTMLInputElement>(null);
    const [step, setStep] = useState<Step>('choose');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [phoneTouched, setPhoneTouched] = useState(false);
    const [code, setCode] = useState('');
    const [wrongCode, setWrongCode] = useState(false);
    const [maxAttempts, setMaxAttempts] = useState(false);
    const [recoveryCode, setRecoveryCode] = useState('');
    const [copied, setCopied] = useState(false);

    const { data: config } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });

    const backup = useMutation({
        mutationFn: generateBackupCode,
        onSuccess: ({ recoveryCode: generated }) => {
            setRecoveryCode(formatRecoveryCode(generated));
            setStep('backup');
        },
    });

    const sendCode = useMutation({
        mutationFn: sendPhoneCode,
        onSuccess: () => {
            setCode('');
            setWrongCode(false);
            setMaxAttempts(false);
            setStep('sms');
        },
    });

    const verifyCode = useMutation({
        mutationFn: verifyPhoneCode,
        onSuccess: () => setStep('congrats'),
        onError: (error) => {
            setCode('');
            if (error instanceof Response && error.status === 429) {
                setMaxAttempts(true);
            } else {
                setWrongCode(true);
                // the input is disabled while verifying, focus once enabled
                setTimeout(() => otpRef.current?.focus(), FOCUS_DELAY);
            }
        },
    });

    const finish = useMutation({
        mutationFn: finishEnrollment,
        // the login domain sets its cookie and redirects back to the security page
        onSuccess: ({ enrollmentVerificationKey }) =>
            window.location.assign(
                `${config?.accountBaseUrl}/register/${enrollmentVerificationKey}`,
            ),
    });

    const validPhone = PHONE_NUMBER.test(sanitizePhoneNumber(phoneNumber));

    const submitPhone = (event: FormEvent) => {
        event.preventDefault();
        setPhoneTouched(true);
        if (validPhone && !sendCode.isPending) {
            sendCode.mutate(sanitizePhoneNumber(phoneNumber));
        }
    };

    const submitCode = (value: string) => {
        if (value.length === CODE_LENGTH && !verifyCode.isPending) {
            verifyCode.mutate(value);
        }
    };

    const copy = async () => {
        await navigator.clipboard.writeText(recoveryCode);
        setCopied(true);
        setTimeout(() => setCopied(false), COPIED_TIMEOUT);
    };

    // the recovery code is only shown once, so it can not be dismissed by accident
    const locked = step === 'backup' || step === 'congrats';

    return (
        <Dialog open onOpenChange={(open) => !open && !locked && onClose()}>
            <DialogContent showCloseButton={!locked}>
                {step === 'choose' && (
                    <>
                        <DialogHeader>
                            <DialogTitle>
                                {I18n.t('app.recovery.title')}
                            </DialogTitle>
                            <DialogDescription>
                                {I18n.t('app.recovery.info')}
                            </DialogDescription>
                        </DialogHeader>
                        <div className="recovery-dialog__options">
                            <Button
                                variant="outline"
                                className="recovery-dialog__option"
                                onClick={() => setStep('phone')}
                            >
                                <span>
                                    {I18n.t('app.recovery.phone.option')}
                                </span>
                                <span className="recovery-dialog__hint">
                                    {I18n.t('app.recovery.phone.optionInfo')}
                                </span>
                            </Button>
                            <Button
                                variant="outline"
                                className="recovery-dialog__option"
                                disabled={backup.isPending}
                                onClick={() => backup.mutate()}
                            >
                                <span>
                                    {I18n.t('app.recovery.backup.option')}
                                </span>
                                <span className="recovery-dialog__hint">
                                    {I18n.t('app.recovery.backup.optionInfo')}
                                </span>
                            </Button>
                            {backup.isError && (
                                <FieldError>
                                    {I18n.t('common.error')}
                                </FieldError>
                            )}
                        </div>
                    </>
                )}

                {step === 'phone' && (
                    <form onSubmit={submitPhone}>
                        <DialogHeader>
                            <DialogTitle>
                                {I18n.t('app.recovery.phone.title')}
                            </DialogTitle>
                            <DialogDescription>
                                {I18n.t('app.recovery.phone.info')}
                            </DialogDescription>
                        </DialogHeader>
                        <FieldGroup className="recovery-dialog__fields">
                            <Field>
                                <FieldLabel htmlFor="phoneNumber">
                                    {I18n.t('app.recovery.phone.label')}
                                </FieldLabel>
                                <Input
                                    id="phoneNumber"
                                    type="tel"
                                    autoFocus
                                    autoComplete="tel"
                                    placeholder={I18n.t(
                                        'app.recovery.phone.placeholder',
                                    )}
                                    value={phoneNumber}
                                    onChange={(e) =>
                                        setPhoneNumber(e.target.value)
                                    }
                                />
                                {phoneTouched && !validPhone && (
                                    <FieldError>
                                        {I18n.t('app.recovery.phone.incorrect')}
                                    </FieldError>
                                )}
                                {sendCode.isError && (
                                    <FieldError>
                                        {I18n.t(
                                            'app.recovery.phone.rateLimited',
                                        )}
                                    </FieldError>
                                )}
                            </Field>
                        </FieldGroup>
                        <DialogFooter>
                            <Button
                                type="button"
                                variant="outline"
                                onClick={() => setStep('choose')}
                            >
                                {I18n.t('app.recovery.back')}
                            </Button>
                            <Button type="submit" disabled={sendCode.isPending}>
                                {sendCode.isPending && <Spinner />}
                                {I18n.t('app.recovery.phone.send')}
                            </Button>
                        </DialogFooter>
                    </form>
                )}

                {step === 'sms' && (
                    <>
                        <DialogHeader className="recovery-dialog__centered">
                            <DialogTitle>
                                {I18n.t('app.recovery.sms.title')}
                            </DialogTitle>
                            <DialogDescription>
                                {I18n.t('app.recovery.sms.info')}
                            </DialogDescription>
                        </DialogHeader>
                        <div className="recovery-dialog__code">
                            <InputOTP
                                ref={otpRef}
                                autoFocus
                                maxLength={CODE_LENGTH}
                                pattern="^\d+$"
                                inputMode="numeric"
                                autoComplete="one-time-code"
                                aria-label={I18n.t('app.recovery.sms.label')}
                                value={code}
                                disabled={verifyCode.isPending || maxAttempts}
                                containerClassName="recovery-dialog__otp"
                                onChange={(value) => {
                                    setCode(value);
                                    setWrongCode(false);
                                }}
                                onComplete={submitCode}
                            >
                                <InputOTPGroup className="recovery-dialog__slots">
                                    {Array.from(
                                        { length: CODE_LENGTH },
                                        (_, index) => (
                                            <InputOTPSlot
                                                key={index}
                                                index={index}
                                            />
                                        ),
                                    )}
                                </InputOTPGroup>
                            </InputOTP>
                            {wrongCode && !maxAttempts && (
                                <FieldError>
                                    {I18n.t('app.recovery.sms.incorrect')}
                                </FieldError>
                            )}
                            {maxAttempts && (
                                <FieldError>
                                    {I18n.t('app.recovery.sms.maxAttempts')}
                                </FieldError>
                            )}
                        </div>
                        <DialogFooter>
                            <Button
                                variant="outline"
                                onClick={() => setStep('phone')}
                            >
                                {I18n.t('app.recovery.sms.resend')}
                            </Button>
                            <Button
                                disabled={
                                    code.length < CODE_LENGTH ||
                                    verifyCode.isPending ||
                                    maxAttempts
                                }
                                onClick={() => submitCode(code)}
                            >
                                {verifyCode.isPending && <Spinner />}
                                {I18n.t('app.recovery.sms.confirm')}
                            </Button>
                        </DialogFooter>
                    </>
                )}

                {step === 'backup' && (
                    <>
                        <DialogHeader>
                            <DialogTitle>
                                {I18n.t('app.recovery.backup.title')}
                            </DialogTitle>
                            <DialogDescription>
                                {I18n.t('app.recovery.backup.info')}
                            </DialogDescription>
                        </DialogHeader>
                        <p className="recovery-dialog__recovery-code">
                            {recoveryCode}
                        </p>
                        <DialogFooter>
                            <Button variant="outline" onClick={copy}>
                                {I18n.t(
                                    copied
                                        ? 'app.recovery.backup.copied'
                                        : 'app.recovery.backup.copy',
                                )}
                            </Button>
                            <Button onClick={() => setStep('congrats')}>
                                {I18n.t('app.recovery.backup.next')}
                            </Button>
                        </DialogFooter>
                    </>
                )}

                {step === 'congrats' && (
                    <>
                        <DialogHeader>
                            <DialogTitle>
                                {I18n.t('app.recovery.congrats.title')}
                            </DialogTitle>
                            <DialogDescription>
                                {I18n.t('app.recovery.congrats.info')}
                            </DialogDescription>
                        </DialogHeader>
                        {finish.isError && (
                            <FieldError>{I18n.t('common.error')}</FieldError>
                        )}
                        <DialogFooter>
                            <Button
                                disabled={finish.isPending || finish.isSuccess}
                                onClick={() => finish.mutate()}
                            >
                                {(finish.isPending || finish.isSuccess) && (
                                    <Spinner />
                                )}
                                {I18n.t('app.recovery.congrats.next')}
                            </Button>
                        </DialogFooter>
                    </>
                )}
            </DialogContent>
        </Dialog>
    );
};
