import { useRef, useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate } from 'react-router';

import { toast } from 'sonner';

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
    Spinner,
} from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import {
    fetchConfig,
    generateEmailCode,
    logout,
    resendEmailCode,
    verifyEmailCode,
} from '../api';
import type { User } from '../api';
import I18n from '../locale/I18n';
import './EditEmailDialog.scss';

// the input is re-enabled after the failed request has settled
const FOCUS_DELAY = 50;
const CODE_LENGTH = 6;
const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

type Step = 'email' | 'outstanding' | 'code';

type EditEmailDialogProps = {
    user: User;
    onClose: () => void;
};

const isRateLimited = (error: unknown) =>
    error instanceof Response && (error.status === 400 || error.status === 403);

// mounted only while open, so every change starts at the first step
export const EditEmailDialog = ({ user, onClose }: EditEmailDialogProps) => {
    const navigate = useNavigate();
    const otpRef = useRef<HTMLInputElement>(null);
    const [step, setStep] = useState<Step>('email');
    const [email, setEmail] = useState('');
    const [touched, setTouched] = useState(false);
    const [duplicate, setDuplicate] = useState(false);
    const [code, setCode] = useState('');
    const [wrongCode, setWrongCode] = useState(false);
    const [canResend, setCanResend] = useState(false);
    const [resent, setResent] = useState(false);

    const { data: config } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });
    const resendDelay = (config?.emailSpamThresholdSeconds ?? 0) * 1000;

    const trimmed = email.trim();
    const sameAsCurrent = trimmed.toLowerCase() === user.email.toLowerCase();
    const valid = EMAIL.test(trimmed) && !sameAsCurrent;

    // too many wrong codes: the server locks the account, so log out
    const logoutAfterRateLimit = async () => {
        await logout();
        window.location.assign(
            `${config?.accountBaseUrl ?? ''}/doLogout?param=${encodeURIComponent('ratelimit=true')}`,
        );
    };

    const allowResendLater = () => {
        setCanResend(false);
        setTimeout(() => setCanResend(true), resendDelay);
    };

    const requestCode = useMutation({
        mutationFn: (force: boolean) => generateEmailCode(trimmed, force),
        onSuccess: () => {
            setCode('');
            setWrongCode(false);
            setResent(false);
            setStep('code');
            allowResendLater();
            toast.info(I18n.t('toast.emailCodeSent', { email: trimmed }));
        },
        onError: (error) => {
            if (error instanceof Response && error.status === 409) {
                setDuplicate(true);
                setStep('email');
            } else if (error instanceof Response && error.status === 406) {
                setStep('outstanding');
            } else {
                void logoutAfterRateLimit();
            }
        },
    });

    const resend = useMutation({
        mutationFn: resendEmailCode,
        onSuccess: () => {
            setResent(true);
            allowResendLater();
        },
        onError: logoutAfterRateLimit,
    });

    const verify = useMutation({
        mutationFn: verifyEmailCode,
        // the confirmation page applies the change
        onSuccess: ({ hash }) =>
            navigate(
                `/update-email?h=${encodeURIComponent(hash)}&nav=security`,
            ),
        onError: (error) => {
            if (isRateLimited(error)) {
                void logoutAfterRateLimit();
            } else {
                setWrongCode(true);
                setCode('');
                // the input is disabled while verifying, focus once enabled
                setTimeout(() => otpRef.current?.focus(), FOCUS_DELAY);
            }
        },
    });

    const submitEmail = (event: FormEvent) => {
        event.preventDefault();
        setTouched(true);
        setDuplicate(false);
        if (valid && !requestCode.isPending) {
            requestCode.mutate(false);
        }
    };

    const submitCode = (value: string) => {
        if (value.length === CODE_LENGTH && !verify.isPending) {
            verify.mutate(value);
        }
    };

    const cancel = (
        <Button type="button" variant="outline" onClick={onClose}>
            {I18n.t('security.emailFlow.cancel')}
        </Button>
    );

    return (
        <Dialog open onOpenChange={(open) => !open && onClose()}>
            <DialogContent>
                {step === 'email' && (
                    <form className="edit-email__form" onSubmit={submitEmail}>
                        <DialogHeader>
                            <DialogTitle className="edit-email__title">
                                {I18n.t('security.emailFlow.title')}
                            </DialogTitle>
                        </DialogHeader>
                        <Field>
                            <FieldLabel htmlFor="newEmail">
                                {I18n.t('security.emailFlow.newEmail')}
                            </FieldLabel>
                            <Input
                                id="newEmail"
                                data-testid="edit-email-input"
                                type="email"
                                autoFocus
                                autoComplete="email"
                                value={email}
                                onChange={(e) => {
                                    setEmail(e.target.value);
                                    setDuplicate(false);
                                }}
                            />
                            <FieldDescription>
                                {I18n.t('security.emailFlow.hint')}
                            </FieldDescription>
                            {touched && !valid && (
                                <FieldError>
                                    {I18n.t(
                                        sameAsCurrent
                                            ? 'security.emailFlow.same'
                                            : 'security.emailFlow.invalid',
                                    )}
                                </FieldError>
                            )}
                            {duplicate && (
                                <FieldError>
                                    {I18n.t('security.emailFlow.duplicate')}
                                </FieldError>
                            )}
                        </Field>
                        <DialogFooter>
                            {cancel}
                            <Button
                                type="submit"
                                data-testid="edit-email-request"
                                disabled={requestCode.isPending}
                            >
                                {requestCode.isPending && <Spinner />}
                                {I18n.t('security.emailFlow.request')}
                            </Button>
                        </DialogFooter>
                    </form>
                )}

                {step === 'outstanding' && (
                    <>
                        <DialogHeader>
                            <DialogTitle className="edit-email__title">
                                {I18n.t('security.emailFlow.outstandingTitle')}
                            </DialogTitle>
                            <DialogDescription>
                                {I18n.t('security.emailFlow.outstandingInfo')}
                            </DialogDescription>
                        </DialogHeader>
                        <DialogFooter>
                            {cancel}
                            <Button
                                disabled={requestCode.isPending}
                                onClick={() => requestCode.mutate(true)}
                            >
                                {I18n.t('security.emailFlow.confirm')}
                            </Button>
                        </DialogFooter>
                    </>
                )}

                {step === 'code' && (
                    <form
                        className="edit-email__form"
                        onSubmit={(event) => {
                            event.preventDefault();
                            submitCode(code);
                        }}
                    >
                        <DialogHeader className="edit-email__centered">
                            <DialogTitle className="edit-email__title">
                                {I18n.t('security.emailFlow.codeTitle')}
                            </DialogTitle>
                        </DialogHeader>
                        <div className="edit-email__code">
                            <p className="edit-email__sent">
                                {I18n.t('security.emailFlow.codeSentTo')}
                                <strong>{trimmed}</strong>
                            </p>
                            <InputOTP
                                ref={otpRef}
                                autoFocus
                                data-testid="edit-email-code"
                                maxLength={CODE_LENGTH}
                                pattern="^\d+$"
                                inputMode="numeric"
                                autoComplete="one-time-code"
                                aria-label={I18n.t(
                                    'security.emailFlow.codeLabel',
                                )}
                                value={code}
                                disabled={verify.isPending}
                                containerClassName="edit-email__otp"
                                onChange={(value) => {
                                    setCode(value);
                                    setWrongCode(false);
                                }}
                                onComplete={submitCode}
                            >
                                <InputOTPGroup className="edit-email__slots">
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
                            {wrongCode && (
                                <FieldError>
                                    {I18n.t('security.emailFlow.wrongCode')}
                                </FieldError>
                            )}
                        </div>
                        <DialogFooter className="edit-email__confirm">
                            <Button
                                type="submit"
                                disabled={
                                    code.length < CODE_LENGTH ||
                                    verify.isPending
                                }
                            >
                                {verify.isPending && <Spinner />}
                                {I18n.t('security.emailFlow.confirm')}
                            </Button>
                        </DialogFooter>
                        <p className="edit-email__resend">
                            {resent
                                ? I18n.t('security.emailFlow.resent')
                                : null}{' '}
                            {canResend && (
                                <>
                                    {I18n.t('security.emailFlow.resendPrompt')}{' '}
                                    <Button
                                        type="button"
                                        variant="link"
                                        className="edit-email__link"
                                        disabled={resend.isPending}
                                        onClick={() => resend.mutate()}
                                    >
                                        {I18n.t('security.emailFlow.resend')}
                                    </Button>
                                </>
                            )}
                        </p>
                    </form>
                )}
            </DialogContent>
        </Dialog>
    );
};
