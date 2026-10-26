import { useCallback, useEffect, useRef, useState } from 'react';

import { logout } from '../api';

export const CODE_LENGTH = 6;

// the input is re-enabled after the failed request has settled
const FOCUS_DELAY = 50;

export const isRateLimited = (error: unknown) =>
    error instanceof Response && (error.status === 400 || error.status === 403);

// too many wrong codes: the server locks the account, so log out
export const logoutAfterRateLimit = async (accountBaseUrl: string) => {
    await logout();
    window.location.assign(
        `${accountBaseUrl}/doLogout?param=${encodeURIComponent('ratelimit=true')}`,
    );
};

// state shared by the flows that verify a code sent by mail
export const useCodeVerification = (resendDelay: number) => {
    const otpRef = useRef<HTMLInputElement>(null);
    // after a wrong code the user has to confirm explicitly
    const autoSubmit = useRef(true);
    const resendTimer = useRef<ReturnType<typeof setTimeout>>(undefined);
    const [code, setCode] = useState('');
    const [wrongCode, setWrongCode] = useState(false);
    const [canResend, setCanResend] = useState(false);
    const [resent, setResent] = useState(false);

    useEffect(() => () => clearTimeout(resendTimer.current), []);

    const allowResendLater = useCallback(() => {
        clearTimeout(resendTimer.current);
        setCanResend(false);
        resendTimer.current = setTimeout(() => setCanResend(true), resendDelay);
    }, [resendDelay]);

    // a new code was sent
    const reset = () => {
        setCode('');
        autoSubmit.current = true;
        setWrongCode(false);
        setResent(false);
        allowResendLater();
    };

    const markResent = () => {
        setResent(true);
        allowResendLater();
    };

    const markWrongCode = () => {
        setWrongCode(true);
        autoSubmit.current = false;
        // the input is disabled while verifying, focus once enabled
        setTimeout(() => otpRef.current?.focus(), FOCUS_DELAY);
    };

    const changeCode = (value: string) => {
        setCode(value);
        setWrongCode(false);
    };

    return {
        otpRef,
        autoSubmit,
        code,
        wrongCode,
        canResend,
        resent,
        allowResendLater,
        reset,
        markResent,
        markWrongCode,
        changeCode,
    };
};
