import { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router';

import {
    Button,
    FieldError,
    InputOTP,
    InputOTPGroup,
    InputOTPSlot,
    Spinner,
} from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import {
    fetchConfig,
    resendInstitutionMail,
    verifyInstitutionEduId,
} from '../../api';
import { GuestLayout } from '../../components/GuestLayout';
import I18n from '../../locale/I18n';
import './route.scss';

// the input is re-enabled after the failed request has settled
const FOCUS_DELAY = 50;
const CODE_LENGTH = 6;

type VerifyState = { email?: string; newUser?: boolean } | null;

// public, the code in the mail proves that the email address is the user's
export default function VerifyFromInstitution() {
    const { hash = '' } = useParams();
    const navigate = useNavigate();
    const state = useLocation().state as VerifyState;
    const otpRef = useRef<HTMLInputElement>(null);
    const [code, setCode] = useState('');
    const [wrongCode, setWrongCode] = useState(false);
    const [canResend, setCanResend] = useState(false);
    const [resent, setResent] = useState(false);

    const { data: config } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });
    const resendDelay = (config?.emailSpamThresholdSeconds ?? 0) * 1000;

    const expire = () =>
        navigate('/create-from-institution/expired', { replace: true });

    const allowResendLater = () => {
        setCanResend(false);
        setTimeout(() => setCanResend(true), resendDelay);
    };

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
                setWrongCode(true);
                setCode('');
                // the input is disabled while verifying, focus once enabled
                setTimeout(() => otpRef.current?.focus(), FOCUS_DELAY);
            }
        },
    });

    const resend = useMutation({
        mutationFn: () => resendInstitutionMail(hash),
        onSuccess: () => {
            setResent(true);
            allowResendLater();
        },
        onError: expire,
    });

    // the first resend is possible after the delay from the mail already sent
    useEffect(() => {
        const timer = setTimeout(() => setCanResend(true), resendDelay);
        return () => clearTimeout(timer);
    }, [resendDelay]);

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
                <InputOTP
                    ref={otpRef}
                    autoFocus
                    maxLength={CODE_LENGTH}
                    pattern="^\d+$"
                    inputMode="numeric"
                    autoComplete="one-time-code"
                    aria-label={I18n.t('createFromInstitution.codeLabel')}
                    value={code}
                    disabled={verify.isPending}
                    containerClassName="verify-from-institution__otp"
                    onChange={(value) => {
                        setCode(value);
                        setWrongCode(false);
                    }}
                    onComplete={submit}
                >
                    <InputOTPGroup className="verify-from-institution__slots">
                        {Array.from({ length: CODE_LENGTH }, (_, index) => (
                            <InputOTPSlot key={index} index={index} />
                        ))}
                    </InputOTPGroup>
                </InputOTP>
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
                <p>
                    {resent ? I18n.t('createFromInstitution.resent') : null}{' '}
                    {canResend && (
                        <>
                            {I18n.t('createFromInstitution.resendPrompt')}{' '}
                            <Button
                                type="button"
                                variant="link"
                                className="verify-from-institution__link"
                                disabled={resend.isPending}
                                onClick={() => resend.mutate()}
                            >
                                {I18n.t('createFromInstitution.resend')}
                            </Button>
                        </>
                    )}
                </p>
            </form>
        </GuestLayout>
    );
}
