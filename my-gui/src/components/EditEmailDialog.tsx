import { useState } from 'react';
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
    Spinner,
} from '@surfnet/curve-react';
import { useMutation } from '@tanstack/react-query';

import { generateEmailCode, resendEmailCode, verifyEmailCode } from '../api';
import type { User } from '../api';
import {
    CODE_LENGTH,
    isRateLimited,
    logoutAfterRateLimit,
    useCodeVerification,
} from '../hooks/useCodeVerification';
import I18n from '../locale/I18n';
import { useConfigStore } from '../store/config';
import { CodeResend } from './CodeResend';
import './EditEmailDialog.scss';
import { OtpInput } from './OtpInput';

const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

type Step = 'email' | 'outstanding' | 'code';

type EditEmailDialogProps = {
    user: User;
    onClose: () => void;
};

// mounted only while open, so every change starts at the first step
export const EditEmailDialog = ({ user, onClose }: EditEmailDialogProps) => {
    const navigate = useNavigate();
    const [step, setStep] = useState<Step>('email');
    const [email, setEmail] = useState('');
    const [touched, setTouched] = useState(false);
    const [duplicate, setDuplicate] = useState(false);

    const config = useConfigStore((state) => state.config);
    const resendDelay = (config?.emailSpamThresholdSeconds ?? 0) * 1000;
    const {
        otpRef,
        autoSubmit,
        code,
        wrongCode,
        canResend,
        resent,
        reset,
        markResent,
        markWrongCode,
        changeCode,
    } = useCodeVerification(resendDelay);

    const trimmed = email.trim();
    const sameAsCurrent = trimmed.toLowerCase() === user.email.toLowerCase();
    const valid = EMAIL.test(trimmed) && !sameAsCurrent;

    const logoutRateLimited = () =>
        logoutAfterRateLimit(config?.accountBaseUrl ?? '');

    const requestCode = useMutation({
        mutationFn: (force: boolean) => generateEmailCode(trimmed, force),
        onSuccess: () => {
            reset();
            setStep('code');
            toast.info(I18n.t('toast.emailCodeSent', { email: trimmed }));
        },
        onError: (error) => {
            if (error instanceof Response && error.status === 409) {
                setDuplicate(true);
                setStep('email');
            } else if (error instanceof Response && error.status === 406) {
                setStep('outstanding');
            } else {
                void logoutRateLimited();
            }
        },
    });

    const resend = useMutation({
        mutationFn: resendEmailCode,
        onSuccess: markResent,
        onError: logoutRateLimited,
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
                void logoutRateLimited();
            } else {
                markWrongCode();
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
                            <OtpInput
                                ref={otpRef}
                                testId="edit-email-code"
                                ariaLabel={I18n.t(
                                    'security.emailFlow.codeLabel',
                                )}
                                value={code}
                                disabled={verify.isPending}
                                containerClassName="edit-email__otp"
                                slotsClassName="edit-email__slots"
                                onChange={changeCode}
                                onComplete={(value) => {
                                    if (autoSubmit.current) {
                                        submitCode(value);
                                    }
                                }}
                            />
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
                        <CodeResend
                            className="edit-email__resend"
                            linkClassName="edit-email__link"
                            resent={resent}
                            canResend={canResend}
                            pending={resend.isPending}
                            resentLabel={I18n.t('security.emailFlow.resent')}
                            promptLabel={I18n.t(
                                'security.emailFlow.resendPrompt',
                            )}
                            resendLabel={I18n.t('security.emailFlow.resend')}
                            onResend={() => resend.mutate()}
                        />
                    </form>
                )}
            </DialogContent>
        </Dialog>
    );
};
