import { useEffect } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router';

import { OtpInput } from '@myconext/shared-gui';
import { Button, FieldError, Spinner } from '@surfnet/curve-react';
import { useMutation } from '@tanstack/react-query';

import { resendInstitutionMail, verifyInstitutionEduId } from '../../api';
import { CodeResend } from '../../components/CodeResend';
import { GuestLayout } from '../../components/GuestLayout';
import {
    CODE_LENGTH,
    useCodeVerification,
} from '../../hooks/useCodeVerification';
import I18n from '../../locale/I18n';
import { useConfigStore } from '../../store/config';
import './route.scss';

type VerifyState = { email?: string; newUser?: boolean } | null;

// public, the code in the mail proves that the email address is the user's
export default function VerifyFromInstitution() {
    const { hash = '' } = useParams();
    const navigate = useNavigate();
    const state = useLocation().state as VerifyState;

    const config = useConfigStore((state) => state.config);
    const resendDelay = (config?.emailSpamThresholdSeconds ?? 0) * 1000;
    const {
        otpRef,
        autoSubmit,
        code,
        wrongCode,
        canResend,
        resent,
        allowResendLater,
        markResent,
        markWrongCode,
        changeCode,
    } = useCodeVerification(resendDelay);

    const expire = () =>
        navigate('/create-from-institution/expired', { replace: true });

    const verify = useMutation({
        mutationFn: (value: string) => verifyInstitutionEduId(hash, value),
        onSuccess: ({ location }) => {
            if (location) {
                window.location.assign(location);
            } else {
                void navigate(`/security?new=${state?.newUser ?? true}`, {
                    replace: true,
                });
            }
        },
        onError: (error) => {
            if (
                error instanceof Response &&
                (error.status === 403 || error.status === 400)
            ) {
                void expire();
            } else {
                markWrongCode();
            }
        },
    });

    const resend = useMutation({
        mutationFn: () => resendInstitutionMail(hash),
        onSuccess: markResent,
        onError: expire,
    });

    // the first resend is possible after the delay from the mail already sent
    useEffect(allowResendLater, [allowResendLater]);

    const submit = (value: string) => {
        if (value.length === CODE_LENGTH && !verify.isPending) {
            verify.mutate(value);
        }
    };

    return (
        <GuestLayout>
            <form
                className="verify-from-institution"
                onSubmit={(event) => {
                    event.preventDefault();
                    submit(code);
                }}
            >
                <h1>{I18n.t('createFromInstitution.verifyTitle')}</h1>
                <p>
                    {I18n.t('createFromInstitution.verifyInfo')}{' '}
                    {state?.email && <strong>{state.email}</strong>}
                </p>
                <OtpInput
                    ref={otpRef}
                    testId="verify-from-institution-code"
                    ariaLabel={I18n.t('createFromInstitution.codeLabel')}
                    value={code}
                    disabled={verify.isPending}
                    onChange={changeCode}
                    onComplete={(value) => {
                        if (autoSubmit.current) {
                            submit(value);
                        }
                    }}
                />
                {wrongCode && (
                    <FieldError>
                        {I18n.t('createFromInstitution.wrongCode')}
                    </FieldError>
                )}
                <Button
                    type="submit"
                    disabled={code.length < CODE_LENGTH || verify.isPending}
                >
                    {verify.isPending && <Spinner />}
                    {I18n.t('createFromInstitution.confirm')}
                </Button>
                <CodeResend
                    className=""
                    linkClassName="verify-from-institution__link"
                    resent={resent}
                    canResend={canResend}
                    pending={resend.isPending}
                    resentLabel={I18n.t('createFromInstitution.resent')}
                    promptLabel={I18n.t('createFromInstitution.resendPrompt')}
                    resendLabel={I18n.t('createFromInstitution.resend')}
                    onResend={() => resend.mutate()}
                />
            </form>
        </GuestLayout>
    );
}
