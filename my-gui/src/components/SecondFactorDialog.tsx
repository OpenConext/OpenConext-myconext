import { useEffect, useRef, useState } from 'react';

import {
    Button,
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    FieldError,
    InputOTP,
    InputOTPGroup,
    InputOTPSlot,
    Spinner,
} from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import {
    confirmStepUp,
    manualResponse,
    pollAuthentication,
    startAuthentication,
} from '../api';
import I18n from '../locale/I18n';
import './SecondFactorDialog.scss';

type SecondFactorDialogProps = {
    // called once the app confirmed, the action can continue
    onConfirmed: () => void;
    onClose: () => void;
};

const POLL_INTERVAL = 1000;
// the server keeps an authentication open for 15 minutes
const MAX_POLLS = 15 * 60;
const CODE_LENGTH = 6;
// the input is re-enabled after the failed request has settled
const FOCUS_DELAY = 50;

const isTouchDevice = () => 'ontouchstart' in document.documentElement;

const suspensionMinutes = (suspendedUntil: number, now: number) => {
    const minutes = Math.round((suspendedUntil - now) / 1000 / 60);
    return minutes < 1 ? 0 : minutes;
};

// mounted only while open, every confirmation starts a new authentication
export const SecondFactorDialog = ({
    onConfirmed,
    onClose,
}: SecondFactorDialogProps) => {
    const [now] = useState(() => Date.now());
    const [showQr, setShowQr] = useState(false);
    const [showCode, setShowCode] = useState(false);
    const [code, setCode] = useState('');
    const [wrongCode, setWrongCode] = useState(false);
    const otpRef = useRef<HTMLInputElement>(null);
    const confirmed = useRef(false);
    const polls = useRef(0);

    // a POST, but it is the first thing the dialog needs and runs only once
    const { data: authentication, isError } = useQuery({
        queryKey: ['stepUp'],
        queryFn: startAuthentication,
        staleTime: Infinity,
        gcTime: 0,
        retry: false,
        refetchOnWindowFocus: false,
    });
    const sessionKey = authentication?.sessionKey;

    const { data: poll } = useQuery({
        queryKey: ['stepUp', sessionKey],
        queryFn: async () => {
            const result = await pollAuthentication(sessionKey!);
            polls.current += 1;
            if (result.status === 'SUCCESS') {
                await confirmStepUp(sessionKey!);
            }
            return { ...result, polls: polls.current };
        },
        enabled: Boolean(sessionKey),
        refetchInterval: (query) =>
            query.state.data?.status === 'SUCCESS' ||
            (query.state.data?.polls ?? 0) >= MAX_POLLS
                ? false
                : POLL_INTERVAL,
        gcTime: 0,
    });

    const timedOut = (poll?.polls ?? 0) >= MAX_POLLS;
    const success = poll?.status === 'SUCCESS';

    useEffect(() => {
        if (success && !confirmed.current) {
            confirmed.current = true;
            onConfirmed();
        }
    }, [success, onConfirmed]);

    const verify = useMutation({
        mutationFn: (response: string) => manualResponse(sessionKey!, response),
        onError: () => {
            setCode('');
            setWrongCode(true);
            // the input is disabled while verifying, focus once enabled
            setTimeout(() => otpRef.current?.focus(), FOCUS_DELAY);
        },
    });

    const changeCode = (value: string) => {
        setCode(value);
        setWrongCode(false);
    };

    const qrVisible =
        authentication && (!authentication.tiqrCookiePresent || showQr);
    const suspended = poll?.status === 'SUSPENDED';
    const minutes =
        suspended && poll.suspendedUntil
            ? suspensionMinutes(poll.suspendedUntil, now)
            : 0;

    const title = timedOut
        ? 'app.stepUp.timeoutTitle'
        : qrVisible
          ? 'app.stepUp.scanTitle'
          : 'app.stepUp.pushTitle';

    return (
        <Dialog open onOpenChange={(open) => !open && onClose()}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>
                        {I18n.t(isError ? 'common.error' : title)}
                    </DialogTitle>
                    {!isError && (timedOut || !qrVisible) && (
                        <DialogDescription>
                            {I18n.t(
                                timedOut
                                    ? 'app.stepUp.timeoutInfo'
                                    : 'app.stepUp.pushInfo',
                            )}
                        </DialogDescription>
                    )}
                </DialogHeader>

                {!authentication && !isError && (
                    <div className="second-factor__center">
                        <Spinner />
                    </div>
                )}

                {authentication && !timedOut && (
                    <>
                        {qrVisible ? (
                            <div className="second-factor__center">
                                <img
                                    className="second-factor__qr"
                                    src={authentication.qr}
                                    alt={I18n.t('app.stepUp.qr')}
                                />
                                {isTouchDevice() && (
                                    <Button
                                        nativeButton={false}
                                        render={<a href={authentication.url} />}
                                        variant="outline"
                                    >
                                        {I18n.t('app.stepUp.open')}
                                    </Button>
                                )}
                            </div>
                        ) : (
                            <div className="second-factor__center">
                                <Spinner />
                            </div>
                        )}

                        {suspended && (
                            <FieldError>
                                {I18n.t('app.stepUp.suspended')}{' '}
                                {minutes > 0
                                    ? I18n.t('app.stepUp.suspendedMinutes', {
                                          count: minutes,
                                      })
                                    : I18n.t('app.stepUp.notSuspended')}
                            </FieldError>
                        )}

                        <div className="second-factor__help">
                            {!qrVisible && (
                                <p>
                                    <strong>
                                        {I18n.t('app.stepUp.noNotification')}
                                    </strong>{' '}
                                    <button
                                        type="button"
                                        className="second-factor__link"
                                        onClick={() => setShowQr(true)}
                                    >
                                        {I18n.t('app.stepUp.qrLink')}
                                    </button>{' '}
                                    {I18n.t('app.stepUp.qrPostfix')}
                                </p>
                            )}
                            {qrVisible && (
                                <p>
                                    {I18n.t('app.stepUp.offline')}{' '}
                                    <button
                                        type="button"
                                        className="second-factor__link"
                                        onClick={() => setShowCode(!showCode)}
                                    >
                                        {I18n.t('app.stepUp.offlineLink')}
                                    </button>
                                </p>
                            )}
                            {qrVisible && showCode && (
                                <InputOTP
                                    ref={otpRef}
                                    autoFocus
                                    maxLength={CODE_LENGTH}
                                    pattern="^\d+$"
                                    aria-label={I18n.t('app.stepUp.codeLabel')}
                                    value={code}
                                    disabled={verify.isPending}
                                    containerClassName="second-factor__otp"
                                    onChange={changeCode}
                                    onComplete={(value) => verify.mutate(value)}
                                >
                                    <InputOTPGroup className="second-factor__slots">
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
                                    {I18n.t('app.stepUp.wrongCode')}
                                </FieldError>
                            )}
                            <p>
                                <strong>{I18n.t('app.stepUp.lost')}</strong>{' '}
                                <a
                                    href={I18n.t('app.stepUp.lostUrl')}
                                    target="_blank"
                                    rel="noreferrer"
                                >
                                    {I18n.t('app.stepUp.lostLink')}
                                </a>
                            </p>
                        </div>
                    </>
                )}

                <DialogFooter>
                    <Button variant="outline" onClick={onClose}>
                        {I18n.t('app.stepUp.cancel')}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};
