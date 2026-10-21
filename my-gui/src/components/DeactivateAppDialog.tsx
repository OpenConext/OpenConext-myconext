import { useRef, useState } from 'react';
import type { FormEvent } from 'react';

import { LinkBreakIcon } from '@phosphor-icons/react';
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogMedia,
    AlertDialogTitle,
    FieldError,
    Input,
    InputOTP,
    InputOTPGroup,
    InputOTPSlot,
    Spinner,
} from '@surfnet/curve-react';
import { useMutation, useQueryClient } from '@tanstack/react-query';

import { deactivateApp, sendDeactivationCode } from '../api';
import type { User } from '../api';
import I18n from '../locale/I18n';
import './DeactivateAppDialog.scss';

// the input is re-enabled after the failed request has settled
const FOCUS_DELAY = 50;

type DeactivateAppDialogProps = {
    user: User;
    onClose: () => void;
};

const CODE_LENGTH = 6;

// mounted only while open, so every flow starts at the first step
export const DeactivateAppDialog = ({
    user,
    onClose,
}: DeactivateAppDialogProps) => {
    const queryClient = useQueryClient();
    // users that saved a recovery code type it instead of getting a code sent
    const useRecoveryCode = Boolean(user.registration?.recoveryCode);
    const otpRef = useRef<HTMLInputElement>(null);

    const [step, setStep] = useState<'intro' | 'code'>('intro');
    const [code, setCode] = useState('');
    const [wrongCode, setWrongCode] = useState(false);
    const [maxAttempts, setMaxAttempts] = useState(false);

    const sendCode = useMutation({
        mutationFn: sendDeactivationCode,
        onSuccess: () => setStep('code'),
    });

    const deactivate = useMutation({
        mutationFn: deactivateApp,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['me'] });
            onClose();
        },
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

    const handleContinue = () => {
        if (useRecoveryCode) {
            setStep('code');
        } else {
            sendCode.mutate();
        }
    };

    const submit = (value: string) => {
        if (value.trim() !== '' && !deactivate.isPending && !maxAttempts) {
            deactivate.mutate(value.trim());
        }
    };

    const changeCode = (value: string) => {
        setCode(value);
        setWrongCode(false);
    };

    const handleSubmit = (event: FormEvent) => {
        event.preventDefault();
        submit(code);
    };

    const complete = useRecoveryCode
        ? code.trim() !== ''
        : code.length === CODE_LENGTH;

    return (
        <AlertDialog open onOpenChange={(open) => !open && onClose()}>
            <AlertDialogContent>
                <AlertDialogHeader>
                    <AlertDialogMedia>
                        <LinkBreakIcon />
                    </AlertDialogMedia>
                    <AlertDialogTitle>
                        {I18n.t('app.deactivate.title')}
                    </AlertDialogTitle>
                    <AlertDialogDescription
                        render={<div className="deactivate-app__text" />}
                    >
                        <p>{I18n.t('app.deactivate.info')}</p>
                        <p>
                            {I18n.t(
                                useRecoveryCode
                                    ? 'app.deactivate.recoveryInfo'
                                    : step === 'intro'
                                      ? 'app.deactivate.sendInfo'
                                      : 'app.deactivate.codeInfo',
                            )}
                        </p>
                    </AlertDialogDescription>
                </AlertDialogHeader>
                {step === 'code' && (
                    <form
                        id="deactivate-app-form"
                        className="deactivate-app__code"
                        onSubmit={handleSubmit}
                    >
                        {useRecoveryCode ? (
                            <Input
                                autoFocus
                                spellCheck={false}
                                autoComplete="off"
                                aria-label={I18n.t(
                                    'app.deactivate.recoveryLabel',
                                )}
                                value={code}
                                disabled={deactivate.isPending}
                                onChange={(e) => changeCode(e.target.value)}
                            />
                        ) : (
                            <InputOTP
                                ref={otpRef}
                                autoFocus
                                maxLength={CODE_LENGTH}
                                pattern="^\d+$"
                                inputMode="numeric"
                                autoComplete="one-time-code"
                                aria-label={I18n.t('app.deactivate.codeLabel')}
                                value={code}
                                disabled={deactivate.isPending}
                                containerClassName="deactivate-app__otp"
                                onChange={changeCode}
                                onComplete={submit}
                            >
                                <InputOTPGroup className="deactivate-app__slots">
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
                        )}
                        {wrongCode && (
                            <FieldError>
                                {I18n.t('app.deactivate.wrongCode')}
                            </FieldError>
                        )}
                        {maxAttempts && (
                            <FieldError>
                                {I18n.t('app.deactivate.maxAttempts')}
                            </FieldError>
                        )}
                    </form>
                )}
                {sendCode.isError && (
                    <FieldError>
                        {I18n.t('app.deactivate.sendError')}
                    </FieldError>
                )}
                <AlertDialogFooter>
                    <AlertDialogCancel>
                        {I18n.t('app.deactivate.cancel')}
                    </AlertDialogCancel>
                    {step === 'intro' ? (
                        <AlertDialogAction
                            disabled={sendCode.isPending}
                            onClick={handleContinue}
                        >
                            {sendCode.isPending && <Spinner />}
                            {I18n.t('app.deactivate.next')}
                        </AlertDialogAction>
                    ) : (
                        <AlertDialogAction
                            type="submit"
                            form="deactivate-app-form"
                            disabled={
                                !complete || deactivate.isPending || maxAttempts
                            }
                        >
                            {deactivate.isPending && <Spinner />}
                            {I18n.t('app.deactivate.action')}
                        </AlertDialogAction>
                    )}
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
};
