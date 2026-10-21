import { useRef, useState } from 'react';
import type { FormEvent } from 'react';

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
    FieldGroup,
    FieldLabel,
    Input,
    InputOTP,
    InputOTPGroup,
    InputOTPSlot,
    Spinner,
} from '@surfnet/curve-react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import {
    fetchConfig,
    fetchOutstandingEmailLinks,
    generatePasswordCode,
    isPasswordHashValid,
    logout,
    resendPasswordCode,
    setLoginPreference,
    updatePassword,
    verifyPasswordCode,
} from '../api';
import type { User } from '../api';
import I18n from '../locale/I18n';
import './PasswordDialog.scss';

// the input is re-enabled after the failed request has settled
const FOCUS_DELAY = 50;

type PasswordDialogProps = {
    user: User;
    // the hash of the mailed link, the dialog then starts at the new password
    initialHash?: string;
    onClose: () => void;
};

type Step =
    'confirm' | 'outstanding' | 'code' | 'password' | 'delete' | 'expired';

const CODE_LENGTH = 6;
const MIN_PASSWORD_LENGTH = 8;
const MAX_PASSWORD_LENGTH = 128;

// counts code points rather than UTF-16 units, like the server
const passwordLength = (password: string) => Array.from(password).length;

const isRateLimited = (error: unknown) =>
    error instanceof Response && (error.status === 400 || error.status === 403);

// mounted only while open, so every flow starts at the first step
export const PasswordDialog = ({
    user,
    initialHash,
    onClose,
}: PasswordDialogProps) => {
    const queryClient = useQueryClient();
    const usePassword = Boolean(user.usePassword);
    const prefix = usePassword ? 'update' : 'add';

    const otpRef = useRef<HTMLInputElement>(null);
    const [stepState, setStep] = useState<Step>('confirm');
    const [code, setCode] = useState('');
    const [verifiedHash, setHash] = useState('');
    const [wrongCode, setWrongCode] = useState(false);
    const [canResend, setCanResend] = useState(false);
    const [resent, setResent] = useState(false);
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [tooLong, setTooLong] = useState(false);

    const { data: config } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });
    const { data: outstanding } = useQuery({
        queryKey: ['outstandingEmailLinks'],
        queryFn: fetchOutstandingEmailLinks,
        // the answer must be fresh each time the dialog opens
        gcTime: 0,
    });

    const hashCheck = useQuery({
        queryKey: ['passwordHashValid', initialHash],
        queryFn: () => isPasswordHashValid(initialHash!),
        enabled: Boolean(initialHash),
        gcTime: 0,
        retry: false,
    });
    const hash = verifiedHash || initialHash || '';
    // a mailed link skips the code, once its hash is known to be valid
    const step: Step | 'checking' =
        initialHash && stepState === 'confirm'
            ? hashCheck.isPending
                ? 'checking'
                : hashCheck.data
                  ? 'password'
                  : 'expired'
            : stepState;

    const accountBaseUrl = config?.accountBaseUrl ?? '';
    const resendDelay = (config?.emailSpamThresholdSeconds ?? 0) * 1000;

    // too many wrong codes: the server locks the account, so log out
    const logoutAfterRateLimit = async () => {
        await logout();
        window.location.assign(
            `${accountBaseUrl}/doLogout?param=${encodeURIComponent('ratelimit=true')}`,
        );
    };

    const allowResendLater = () => {
        setCanResend(false);
        setTimeout(() => setCanResend(true), resendDelay);
    };

    const sendCode = useMutation({
        mutationFn: generatePasswordCode,
        onSuccess: () => {
            setCode('');
            setWrongCode(false);
            setResent(false);
            setStep('code');
            allowResendLater();
        },
    });

    const resendCode = useMutation({
        mutationFn: resendPasswordCode,
        onSuccess: () => {
            setResent(true);
            allowResendLater();
        },
        onError: logoutAfterRateLimit,
    });

    const verifyCode = useMutation({
        mutationFn: verifyPasswordCode,
        onSuccess: ({ hash: verified }) => {
            setHash(verified);
            setStep('password');
        },
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

    const savePassword = useMutation({
        mutationFn: async () => {
            await updatePassword(user.id, newPassword, hash);
            return setLoginPreference('usePassword');
        },
        // the login preference is applied by the server on this page
        onSuccess: ({ token }) =>
            window.location.assign(
                `${accountBaseUrl}/register/login-preference/${token}`,
            ),
        onError: (error) => {
            if (error instanceof Response && error.status === 413) {
                setTooLong(true);
            } else {
                setStep('expired');
            }
        },
    });

    const removePassword = useMutation({
        mutationFn: () => updatePassword(user.id, null, hash),
        onSuccess: (updated) => {
            queryClient.setQueryData(['me'], updated);
            toast.success(I18n.t('toast.passwordDeleted'));
            onClose();
        },
        onError: () => setStep('expired'),
    });

    const handleConfirm = () => {
        if (outstanding && step === 'confirm') {
            setStep('outstanding');
        } else {
            sendCode.mutate();
        }
    };

    const submitCode = (value: string) => {
        if (value.length === CODE_LENGTH && !verifyCode.isPending) {
            verifyCode.mutate(value);
        }
    };

    const length = passwordLength(newPassword);
    const validPassword =
        length >= MIN_PASSWORD_LENGTH && length <= MAX_PASSWORD_LENGTH;
    const matches = newPassword === confirmPassword;
    const canSave = validPassword && matches && !savePassword.isPending;

    const handleSave = (event: FormEvent) => {
        event.preventDefault();
        if (canSave) {
            savePassword.mutate();
        }
    };

    const cancel = (
        <Button type="button" variant="outline" onClick={onClose}>
            {I18n.t('security.password.flow.cancel')}
        </Button>
    );

    return (
        <Dialog open onOpenChange={(open) => !open && onClose()}>
            <DialogContent>
                {(step === 'confirm' || step === 'outstanding') && (
                    <>
                        <DialogHeader>
                            <DialogTitle>
                                {I18n.t(
                                    step === 'confirm'
                                        ? `security.password.flow.${prefix}Title`
                                        : 'security.password.flow.outstandingTitle',
                                )}
                            </DialogTitle>
                            <DialogDescription>
                                {I18n.t(
                                    step === 'confirm'
                                        ? `security.password.flow.${prefix}Info`
                                        : 'security.password.flow.outstandingInfo',
                                )}
                            </DialogDescription>
                        </DialogHeader>
                        {sendCode.isError && (
                            <FieldError>{I18n.t('common.error')}</FieldError>
                        )}
                        <DialogFooter>
                            {cancel}
                            <Button
                                disabled={sendCode.isPending}
                                onClick={handleConfirm}
                            >
                                {I18n.t('security.password.flow.confirm')}
                            </Button>
                        </DialogFooter>
                    </>
                )}

                {step === 'code' && (
                    <form
                        onSubmit={(event) => {
                            event.preventDefault();
                            submitCode(code);
                        }}
                    >
                        <DialogHeader className="password-dialog__centered">
                            <DialogTitle>
                                {I18n.t('security.password.flow.codeTitle')}
                            </DialogTitle>
                        </DialogHeader>
                        <div className="password-dialog__code">
                            <p className="password-dialog__sent">
                                {I18n.t('security.password.flow.codeSentTo')}
                                <strong>{user.email}</strong>
                            </p>
                            <InputOTP
                                ref={otpRef}
                                autoFocus
                                maxLength={CODE_LENGTH}
                                pattern="^\d+$"
                                inputMode="numeric"
                                autoComplete="one-time-code"
                                aria-label={I18n.t(
                                    'security.password.flow.codeLabel',
                                )}
                                value={code}
                                disabled={verifyCode.isPending}
                                containerClassName="password-dialog__otp"
                                onChange={(value) => {
                                    setCode(value);
                                    setWrongCode(false);
                                }}
                                onComplete={submitCode}
                            >
                                <InputOTPGroup className="password-dialog__slots">
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
                                    {I18n.t('security.password.flow.codeError')}
                                </FieldError>
                            )}
                        </div>
                        <DialogFooter className="password-dialog__confirm">
                            <Button
                                type="submit"
                                disabled={
                                    code.length < CODE_LENGTH ||
                                    verifyCode.isPending
                                }
                            >
                                {verifyCode.isPending && <Spinner />}
                                {I18n.t('security.password.flow.confirm')}
                            </Button>
                        </DialogFooter>
                        <p className="password-dialog__resend">
                            {resent
                                ? I18n.t('security.password.flow.resent')
                                : null}{' '}
                            {canResend && (
                                <>
                                    {I18n.t(
                                        'security.password.flow.resendPrompt',
                                    )}{' '}
                                    <Button
                                        type="button"
                                        variant="link"
                                        className="password-dialog__link"
                                        disabled={resendCode.isPending}
                                        onClick={() => resendCode.mutate()}
                                    >
                                        {I18n.t(
                                            'security.password.flow.resend',
                                        )}
                                    </Button>
                                </>
                            )}
                        </p>
                    </form>
                )}

                {step === 'password' && (
                    <form onSubmit={handleSave}>
                        <DialogHeader>
                            <DialogTitle>
                                {I18n.t(
                                    usePassword
                                        ? 'security.password.flow.changeTitle'
                                        : 'security.password.flow.newTitle',
                                )}
                            </DialogTitle>
                            <DialogDescription>
                                {I18n.t('security.password.flow.disclaimer')}
                            </DialogDescription>
                        </DialogHeader>
                        <FieldGroup className="password-dialog__fields">
                            <input
                                type="hidden"
                                autoComplete="username email"
                                name="username"
                                value={user.email}
                                readOnly
                            />
                            <Field>
                                <FieldLabel htmlFor="newPassword">
                                    {I18n.t(
                                        'security.password.flow.newPassword',
                                    )}
                                </FieldLabel>
                                <Input
                                    id="newPassword"
                                    type="password"
                                    autoComplete="new-password"
                                    spellCheck={false}
                                    autoFocus
                                    value={newPassword}
                                    onChange={(e) => {
                                        setNewPassword(e.target.value);
                                        setTooLong(false);
                                    }}
                                />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="confirmPassword">
                                    {I18n.t(
                                        'security.password.flow.confirmPassword',
                                    )}
                                </FieldLabel>
                                <Input
                                    id="confirmPassword"
                                    type="password"
                                    autoComplete="new-password"
                                    spellCheck={false}
                                    value={confirmPassword}
                                    onChange={(e) =>
                                        setConfirmPassword(e.target.value)
                                    }
                                />
                                {confirmPassword !== '' && !matches && (
                                    <FieldDescription>
                                        {I18n.t(
                                            'security.password.flow.mismatch',
                                        )}
                                    </FieldDescription>
                                )}
                            </Field>
                            {(tooLong || length > MAX_PASSWORD_LENGTH) && (
                                <FieldError>
                                    {I18n.t('security.password.flow.tooLong')}
                                </FieldError>
                            )}
                        </FieldGroup>
                        <DialogFooter>
                            {usePassword && (
                                <Button
                                    type="button"
                                    variant="outline"
                                    className="password-dialog__delete"
                                    onClick={() => setStep('delete')}
                                >
                                    {I18n.t('security.password.flow.delete')}
                                </Button>
                            )}
                            {cancel}
                            <Button type="submit" disabled={!canSave}>
                                {I18n.t('security.password.flow.save')}
                            </Button>
                        </DialogFooter>
                    </form>
                )}

                {step === 'delete' && (
                    <>
                        <DialogHeader>
                            <DialogTitle>
                                {I18n.t('security.password.flow.deleteTitle')}
                            </DialogTitle>
                            <DialogDescription>
                                {I18n.t('security.password.flow.deleteInfo')}
                            </DialogDescription>
                        </DialogHeader>
                        <DialogFooter>
                            <Button
                                variant="outline"
                                onClick={() => setStep('password')}
                            >
                                {I18n.t('security.password.flow.back')}
                            </Button>
                            <Button
                                variant="destructive"
                                disabled={removePassword.isPending}
                                onClick={() => removePassword.mutate()}
                            >
                                {I18n.t('security.password.flow.deleteAction')}
                            </Button>
                        </DialogFooter>
                    </>
                )}

                {step === 'checking' && (
                    <div className="password-dialog__centered">
                        <Spinner />
                    </div>
                )}

                {step === 'expired' && (
                    <>
                        <DialogHeader>
                            <DialogTitle>
                                {I18n.t(
                                    `security.password.flow.${prefix}Title`,
                                )}
                            </DialogTitle>
                            <DialogDescription>
                                {I18n.t('security.password.flow.expired')}
                            </DialogDescription>
                        </DialogHeader>
                        <DialogFooter>
                            {cancel}
                            <Button
                                disabled={sendCode.isPending}
                                onClick={() => sendCode.mutate()}
                            >
                                {I18n.t('security.password.flow.expiredAction')}
                            </Button>
                        </DialogFooter>
                    </>
                )}
            </DialogContent>
        </Dialog>
    );
};
