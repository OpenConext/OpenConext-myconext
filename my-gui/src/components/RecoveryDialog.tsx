import { useEffect, useRef, useState } from 'react';
import type { FormEvent } from 'react';

import { toast } from 'sonner';

import { ArrowLeftIcon, DeviceMobileSpeakerIcon } from '@phosphor-icons/react';
import {
    Button,
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    Field,
    FieldDescription,
    FieldError,
    FieldLabel,
    Input,
    InputOTP,
    InputOTPGroup,
    InputOTPSlot,
    Item,
    ItemContent,
    ItemTitle,
    Spinner,
} from '@surfnet/curve-react';
import { useMutation } from '@tanstack/react-query';

import {
    finishEnrollment,
    generateBackupCode,
    regenerateBackupCode,
    resendPhoneCode,
    reverifyPhoneCode,
    sendPhoneCode,
    verifyPhoneCode,
} from '../api';
import appReady from '../assets/app/app-ready.png';
import I18n from '../locale/I18n';
import { useConfigStore } from '../store/config';
import { refreshUser } from '../store/me';
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
    // change the recovery method of an active app instead of enrolling
    change?: boolean;
    onClose: () => void;
};

// the last step of enrolling the eduID app: a recovery method, then finishing
export const RecoveryDialog = ({
    change = false,
    onClose,
}: RecoveryDialogProps) => {
    const otpRef = useRef<HTMLInputElement>(null);
    // after a wrong code the user has to confirm explicitly
    const autoSubmit = useRef(true);
    const [step, setStep] = useState<Step>('choose');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [phoneTouched, setPhoneTouched] = useState(false);
    const [code, setCode] = useState('');
    const [wrongCode, setWrongCode] = useState(false);
    const [maxAttempts, setMaxAttempts] = useState(false);
    const [recoveryCode, setRecoveryCode] = useState('');
    const [copied, setCopied] = useState(false);

    const config = useConfigStore((state) => state.config);

    // the app stays active, only its recovery method changed
    const finishChange = async () => {
        await refreshUser();
        toast.success(I18n.t('toast.recoveryChanged'));
        onClose();
    };

    const backup = useMutation({
        mutationFn: change ? regenerateBackupCode : generateBackupCode,
        onSuccess: ({ recoveryCode: generated }) => {
            setRecoveryCode(formatRecoveryCode(generated));
            setStep('backup');
        },
    });

    const sendCode = useMutation({
        mutationFn: change ? resendPhoneCode : sendPhoneCode,
        onSuccess: () => {
            setCode('');
            autoSubmit.current = true;
            setWrongCode(false);
            setMaxAttempts(false);
            setStep('sms');
        },
    });

    const verifyCode = useMutation({
        mutationFn: change ? reverifyPhoneCode : verifyPhoneCode,
        onSuccess: () => (change ? finishChange() : setStep('congrats')),
        onError: (error) => {
            if (error instanceof Response && error.status === 429) {
                setCode('');
                setMaxAttempts(true);
            } else {
                setWrongCode(true);
                autoSubmit.current = false;
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

    // enrolling is only completed by finishing every step, and the recovery
    // code is only shown once, so the dialog can not be dismissed (also not
    // with esc) until then
    const locked = !change || step === 'backup' || step === 'congrats';

    // leaving the page would lose the code that is shown once
    useEffect(() => {
        if (step !== 'backup') {
            return;
        }
        const warn = (event: BeforeUnloadEvent) => event.preventDefault();
        window.addEventListener('beforeunload', warn);
        return () => window.removeEventListener('beforeunload', warn);
    }, [step]);

    return (
        <Dialog open onOpenChange={(open) => !open && !locked && onClose()}>
            <DialogContent showCloseButton={!locked}>
                {step === 'choose' && (
                    <>
                        <DialogHeader className="recovery-dialog__title">
                            <DialogTitle>
                                {I18n.t(
                                    change
                                        ? 'app.recovery.changeTitle'
                                        : 'app.recovery.title',
                                )}
                            </DialogTitle>
                            <DialogDescription>
                                {I18n.t(
                                    change
                                        ? 'app.recovery.changeInfo'
                                        : 'app.recovery.info',
                                )}
                            </DialogDescription>
                        </DialogHeader>
                        <Item
                            variant="outline"
                            data-testid="recovery-phone-option"
                            className="recovery-dialog__option"
                            render={<button type="button" />}
                            onClick={() => setStep('phone')}
                        >
                            <DeviceMobileSpeakerIcon />
                            <ItemContent>
                                <ItemTitle>
                                    {I18n.t('app.recovery.phone.option')}
                                </ItemTitle>
                            </ItemContent>
                        </Item>
                        <div className="recovery-dialog__footer">
                            <p>{I18n.t('app.recovery.noPhone')}</p>
                            <button
                                type="button"
                                data-testid="recovery-backup-option"
                                className="recovery-dialog__link"
                                disabled={backup.isPending}
                                onClick={() => backup.mutate()}
                            >
                                {I18n.t('app.recovery.backup.option')}
                            </button>
                            {backup.isError && (
                                <FieldError>
                                    {I18n.t('common.error')}
                                </FieldError>
                            )}
                        </div>
                    </>
                )}

                {step === 'phone' && (
                    <form
                        className="recovery-dialog__form"
                        onSubmit={submitPhone}
                    >
                        <Button
                            type="button"
                            variant="ghost"
                            className="recovery-dialog__back"
                            onClick={() => setStep('choose')}
                        >
                            <ArrowLeftIcon />
                            {I18n.t('app.recovery.back')}
                        </Button>
                        <DialogHeader className="recovery-dialog__title">
                            <DialogTitle>
                                {I18n.t('app.recovery.phone.title')}
                            </DialogTitle>
                            <DialogDescription>
                                {I18n.t('app.recovery.phone.info')}
                            </DialogDescription>
                        </DialogHeader>
                        <Field>
                            <FieldLabel htmlFor="phoneNumber">
                                {I18n.t('app.recovery.phone.label')}
                            </FieldLabel>
                            <Input
                                id="phoneNumber"
                                data-testid="recovery-phone-input"
                                type="tel"
                                autoFocus
                                autoComplete="tel"
                                value={phoneNumber}
                                onChange={(e) => setPhoneNumber(e.target.value)}
                            />
                            <FieldDescription>
                                {I18n.t('app.recovery.phone.hint')}
                            </FieldDescription>
                            {phoneTouched && !validPhone && (
                                <FieldError>
                                    {I18n.t('app.recovery.phone.incorrect')}
                                </FieldError>
                            )}
                            {sendCode.isError && (
                                <FieldError>
                                    {I18n.t('app.recovery.phone.rateLimited')}
                                </FieldError>
                            )}
                        </Field>
                        <Button
                            type="submit"
                            data-testid="recovery-phone-send"
                            disabled={sendCode.isPending}
                        >
                            {sendCode.isPending && <Spinner />}
                            {I18n.t('app.recovery.phone.send')}
                        </Button>
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
                                data-testid="recovery-sms-code"
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
                                onComplete={(value) => {
                                    if (autoSubmit.current) {
                                        submitCode(value);
                                    }
                                }}
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
                                data-testid="recovery-sms-confirm"
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
                        <DialogHeader className="recovery-dialog__title">
                            <DialogTitle>
                                {I18n.t('app.recovery.backup.title')}
                            </DialogTitle>
                            <DialogDescription>
                                {I18n.t('app.recovery.backup.info')}
                            </DialogDescription>
                        </DialogHeader>
                        <p
                            data-testid="recovery-code"
                            className="recovery-dialog__recovery-code"
                        >
                            {recoveryCode}
                        </p>
                        <div className="recovery-dialog__actions">
                            <Button onClick={copy}>
                                {I18n.t(
                                    copied
                                        ? 'app.recovery.backup.copied'
                                        : 'app.recovery.backup.copy',
                                )}
                            </Button>
                            <Button
                                data-testid="recovery-backup-next"
                                variant="outline"
                                onClick={() =>
                                    change
                                        ? void finishChange()
                                        : setStep('congrats')
                                }
                            >
                                {I18n.t('app.recovery.backup.next')}
                            </Button>
                        </div>
                    </>
                )}

                {step === 'congrats' && (
                    <>
                        <DialogHeader className="recovery-dialog__title">
                            <DialogTitle>
                                {I18n.t('app.recovery.congrats.title')}
                            </DialogTitle>
                            <DialogDescription>
                                {I18n.t('app.recovery.congrats.info')}
                            </DialogDescription>
                        </DialogHeader>
                        <img
                            className="recovery-dialog__ready"
                            src={appReady}
                            alt=""
                        />
                        {finish.isError && (
                            <FieldError>{I18n.t('common.error')}</FieldError>
                        )}
                        <Button
                            data-testid="recovery-congrats-next"
                            disabled={finish.isPending || finish.isSuccess}
                            onClick={() => finish.mutate()}
                        >
                            {(finish.isPending || finish.isSuccess) && (
                                <Spinner />
                            )}
                            {I18n.t('app.recovery.congrats.next')}
                        </Button>
                    </>
                )}
            </DialogContent>
        </Dialog>
    );
};
