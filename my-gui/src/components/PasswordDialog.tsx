import { useState } from 'react';
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
    Spinner,
} from '@surfnet/curve-react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import {
    fetchConfig,
    fetchOutstandingEmailLinks,
    generatePasswordCode,
    isPasswordHashValid,
    resendPasswordCode,
    setLoginPreference,
    updatePassword,
    verifyPasswordCode,
} from '../api';
import type { User } from '../api';
import {
    CODE_LENGTH,
    isRateLimited,
    logoutAfterRateLimit,
    useCodeVerification,
} from '../hooks/useCodeVerification';
import I18n from '../locale/I18n';
import { CodeResend } from './CodeResend';
import { OtpInput } from './OtpInput';
import './PasswordDialog.scss';

type PasswordDialogProps = {
    user: User;
    // the hash of the mailed link, the dialog then starts at the new password
    initialHash?: string;
    onClose: () => void;
};

type Step =
    'confirm' | 'outstanding' | 'code' | 'password' | 'delete' | 'expired';

const MIN_PASSWORD_LENGTH = 8;
const MAX_PASSWORD_LENGTH = 128;

// counts code points rather than UTF-16 units, like the server
const passwordLength = (password: string) => Array.from(password).length;

// mounted only while open, so every flow starts at the first step
export const PasswordDialog = ({
    user,
    initialHash,
    onClose,
}: PasswordDialogProps) => {
    const queryClient = useQueryClient();
    const usePassword = Boolean(user.usePassword);
    const prefix = usePassword ? 'update' : 'add';

    const [stepState, setStep] = useState<Step>('confirm');
    const [verifiedHash, setHash] = useState('');
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
    const logoutRateLimited = () => logoutAfterRateLimit(accountBaseUrl);

    const sendCode = useMutation({
        mutationFn: generatePasswordCode,
        onSuccess: () => {
            reset();
            setStep('code');
        },
    });

    const resendCode = useMutation({
        mutationFn: resendPasswordCode,
        onSuccess: markResent,
        onError: logoutRateLimited,
    });

    const verifyCode = useMutation({
        mutationFn: verifyPasswordCode,
        onSuccess: ({ hash: verified }) => {
            setHash(verified);
            setStep('password');
        },
        onError: (error) => {
            if (isRateLimited(error)) {
                void logoutRateLimited();
            } else {
                markWrongCode();
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
                                data-testid="password-confirm"
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
                            <OtpInput
                                ref={otpRef}
                                testId="password-code"
                                ariaLabel={I18n.t(
                                    'security.password.flow.codeLabel',
                                )}
                                value={code}
                                disabled={verifyCode.isPending}
                                containerClassName="password-dialog__otp"
                                slotsClassName="password-dialog__slots"
                                onChange={changeCode}
                                onComplete={(value) => {
                                    if (autoSubmit.current) {
                                        submitCode(value);
                                    }
                                }}
                            />
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
                        <CodeResend
                            className="password-dialog__resend"
                            linkClassName="password-dialog__link"
                            resent={resent}
                            canResend={canResend}
                            pending={resendCode.isPending}
                            resentLabel={I18n.t(
                                'security.password.flow.resent',
                            )}
                            promptLabel={I18n.t(
                                'security.password.flow.resendPrompt',
                            )}
                            resendLabel={I18n.t(
                                'security.password.flow.resend',
                            )}
                            onResend={() => resendCode.mutate()}
                        />
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
                                    data-testid="password-new"
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
                                    data-testid="password-confirm-new"
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
                                    data-testid="password-delete"
                                    onClick={() => setStep('delete')}
                                >
                                    {I18n.t('security.password.flow.delete')}
                                </Button>
                            )}
                            {cancel}
                            <Button
                                type="submit"
                                data-testid="password-save"
                                disabled={!canSave}
                            >
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
                                data-testid="password-delete-confirm"
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
