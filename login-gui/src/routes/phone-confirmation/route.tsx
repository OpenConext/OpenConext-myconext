import { useRef, useState } from 'react';
import {
    Navigate,
    useLocation,
    useNavigate,
    useSearchParams,
} from 'react-router';

import { useMutation, useQuery } from '@tanstack/react-query';

import {
    type SendError,
    configQueryOptions,
    sendPhoneCode,
    toSendError,
    verifyPhoneCode,
} from '../../api';
import { type CodeError, PhoneConfirmationView } from '../../components';
import { rememberLoginMethod } from '../../utils/loginMethods';
import type { PhoneNavigationState } from '../../utils/phoneNumber';
import { getProceedUrl } from '../../utils/sso';

export default function PhoneConfirmation() {
    const navigate = useNavigate();
    const { state } = useLocation() as { state: PhoneNavigationState };
    const [searchParams] = useSearchParams();
    const hash = searchParams.get('h') ?? '';
    const phoneNumber = state?.phoneNumber ?? '';
    const query = `?h=${encodeURIComponent(hash)}`;

    const inputRef = useRef<HTMLInputElement>(null);
    const [code, setCode] = useState('');
    const [codeError, setCodeError] = useState<CodeError | null>(null);
    const [resent, setResent] = useState(false);
    const [resendError, setResendError] = useState<SendError | null>(null);

    const { data: config } = useQuery(configQueryOptions);

    const { mutate: verify, isPending: isVerifying } = useMutation({
        mutationFn: (value: string) => verifyPhoneCode(hash, value),
        onSuccess: ({ redirect }) => {
            const proceedUrl = getProceedUrl(
                new URLSearchParams({ h: hash, redirect }),
                config?.magicLinkUrl ?? '',
            );
            if (!proceedUrl) {
                setCodeError('failed');
                return;
            }

            rememberLoginMethod('useApp');
            window.location.href = proceedUrl;
        },
        onError: (error) => {
            setCode('');
            setCodeError(
                error instanceof Response && error.status === 429
                    ? 'maxAttempts'
                    : error instanceof Response && error.status === 403
                      ? 'incorrect'
                      : 'failed',
            );
            // The input is disabled while verifying and loses focus
            queueMicrotask(() => inputRef.current?.focus());
        },
    });

    const { mutate: resend, isPending: isResending } = useMutation({
        mutationFn: () => sendPhoneCode(hash, phoneNumber),
        onSuccess: () => {
            setCode('');
            setCodeError(null);
            setResent(true);
            setResendError(null);
            queueMicrotask(() => inputRef.current?.focus());
        },
        onError: (error) => {
            const failure = toSendError(error);
            setResent(false);
            if (failure === 'finalized') {
                navigate(`/recovery-code${query}`, { replace: true });
                return;
            }
            setResendError(failure);
        },
    });

    if (!hash) {
        return <Navigate to="/expired" replace />;
    }

    // The number only lives in navigation state, so a fresh visit has to start over
    if (!phoneNumber) {
        return <Navigate to={`/phone-verification${query}`} replace />;
    }

    const goToPhoneVerification = () =>
        navigate(`/phone-verification${query}`, { state: { phoneNumber } });

    const canVerify =
        config !== undefined && !isVerifying && codeError !== 'maxAttempts';
    const isValidForm = canVerify && code.length > 0;

    return (
        <PhoneConfirmationView
            phoneNumber={phoneNumber}
            code={code}
            inputRef={inputRef}
            onCodeChange={setCode}
            onComplete={(value) => {
                if (!canVerify) {
                    return;
                }
                setCodeError(null);
                verify(value);
            }}
            onSubmit={(e) => {
                e.preventDefault();
                if (!isValidForm) {
                    return;
                }
                verify(code);
            }}
            onResend={() => resend()}
            onChangeNumber={goToPhoneVerification}
            ready={config !== undefined}
            isVerifying={isVerifying}
            isResending={isResending}
            resent={resent}
            codeError={codeError}
            resendError={resendError}
        />
    );
}
