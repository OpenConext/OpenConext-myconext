import { type FC, type ReactNode, useEffect, useRef, useState } from 'react';
import { Navigate, useLocation, useNavigate, useParams } from 'react-router';

import Cookies from 'js-cookie';

import { OtpInput } from '@myconext/shared-gui';
import { WarningDiamondIcon } from '@phosphor-icons/react';
import { Alert, AlertDescription, Button } from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import {
    type TiqrPollResponse,
    tiqrManualResponse,
    tiqrPollAuthentication,
    tiqrStartAuthentication,
} from '../../api';
import { LoginContainer } from '../../components';
import I18n from '../../locale/I18n';
import { LOGIN_PREFERENCE_COOKIE } from '../../utils/loginMethods';
import styles from './route.module.scss';

const POLL_INTERVAL_MS = 1000;
const SESSION_TIMEOUT_MS = 15 * 60 * 1000;
const LOGIN_PREFERENCE_APP = 'useApp';
const HELP_URL = 'https://eduid.nl/help';

type PollResult = TiqrPollResponse & { suspendedMinutes: number };

// The server returns epoch milliseconds, or epoch seconds in some code paths
// (TiqrController); the latter lands in the past and yields 0 ("try again").
const toPollResult = (poll: TiqrPollResponse): PollResult => {
    const minutes = Math.round(
        ((poll.suspendedUntil ?? 0) - Date.now()) / 1000 / 60,
    );

    return { ...poll, suspendedMinutes: minutes < 1 ? 0 : minutes };
};

const isTouchDevice = () =>
    typeof document !== 'undefined' &&
    'ontouchstart' in document.documentElement;

const Container: FC<{
    children: ReactNode;
    title: string;
    description?: string;
    email: string;
    onBack: () => void;
    footer?: ReactNode;
}> = ({ children, title, description, email, onBack, footer }) => (
    <LoginContainer
        title={title}
        description={description}
        footer={footer}
        onBack={onBack}
        backLabel={email}
    >
        {children}
    </LoginContainer>
);

export default function UseApp() {
    const { id = '' } = useParams();
    const { state } = useLocation();
    const navigate = useNavigate();
    const email = typeof state?.email === 'string' ? state.email : '';

    const hasFired = useRef(false);
    const [qrRequested, setQrRequested] = useState(false);
    const [codeRequested, setCodeRequested] = useState(false);
    const [code, setCode] = useState('');
    const [timedOut, setTimedOut] = useState(false);

    const goToOptions = () => navigate(`/options/${id}`, { state: { email } });

    const {
        mutate: startAuthentication,
        data: start,
        isError: startFailed,
    } = useMutation({
        mutationFn: () => tiqrStartAuthentication(email, id),
    });

    useEffect(() => {
        if (hasFired.current || !email) {
            return;
        }

        hasFired.current = true;
        startAuthentication();
    }, [email, startAuthentication]);

    const sessionKey = start?.sessionKey ?? '';

    useEffect(() => {
        if (!sessionKey) {
            return;
        }

        const timer = setTimeout(() => setTimedOut(true), SESSION_TIMEOUT_MS);

        return () => clearTimeout(timer);
    }, [sessionKey]);

    const { data: poll, isError: pollFailed } = useQuery({
        queryKey: ['tiqrPoll', sessionKey],
        queryFn: () => tiqrPollAuthentication(sessionKey, id),
        select: toPollResult,
        // Without this, identical polls keep the same reference and `select`
        // would not re-run, freezing the suspension countdown
        structuralSharing: false,
        enabled: sessionKey !== '' && !timedOut,
        gcTime: 0,
        // The app-on-this-device flow backgrounds this tab
        refetchIntervalInBackground: true,
        refetchInterval: (query) =>
            query.state.data?.status === 'SUCCESS' ? false : POLL_INTERVAL_MS,
    });

    useEffect(() => {
        if (poll?.status !== 'SUCCESS' || !poll.redirect || !poll.hash) {
            return;
        }

        Cookies.set(LOGIN_PREFERENCE_COOKIE, LOGIN_PREFERENCE_APP, {
            expires: 365,
            secure: true,
            sameSite: 'Lax',
        });
        window.location.href = `${poll.redirect}?h=${encodeURIComponent(poll.hash)}`;
    }, [poll]);

    const {
        mutate: submitCode,
        isPending: isVerifying,
        isError: codeFailed,
    } = useMutation({
        mutationFn: (value: string) => tiqrManualResponse(sessionKey, value),
        // On success the next poll returns SUCCESS and handles the redirect
        onError: () => setCode(''),
    });

    if (!email) {
        return <Navigate to={`/login/${id}`} replace />;
    }

    const footer = (
        <div className={styles.footer}>
            <span>{I18n.t('useApp.noAccess')}</span>
            <a href={HELP_URL} target="_blank" rel="noopener noreferrer">
                {I18n.t('useApp.reRegister')}
            </a>
            <span>{I18n.t('useApp.or')}</span>
            <Button
                variant="link"
                type="button"
                className={`${styles.link} ${styles.lowercase}`}
                onClick={goToOptions}
            >
                {I18n.t('useApp.otherMethod')}
            </Button>
        </div>
    );

    // Same as the legacy page: a failing poll ends the session
    if (timedOut || pollFailed) {
        return (
            <Container
                title={I18n.t('useApp.timeOutTitle')}
                email={email}
                onBack={goToOptions}
                footer={footer}
            >
                <div className={styles.content}>
                    <p>{I18n.t('useApp.timeOutInfo')}</p>
                    <Button
                        type="button"
                        onClick={() => window.location.reload()}
                    >
                        {I18n.t('useApp.timeOutRetry')}
                    </Button>
                </div>
            </Container>
        );
    }

    if (startFailed) {
        return (
            <Container
                title={I18n.t('useApp.title')}
                email={email}
                onBack={goToOptions}
                footer={footer}
            >
                <Alert variant="warning">
                    <WarningDiamondIcon />
                    <AlertDescription>
                        {I18n.t('useApp.startError')}
                    </AlertDescription>
                </Alert>
            </Container>
        );
    }

    if (!start) {
        return (
            <Container
                title={I18n.t('useApp.title')}
                email={email}
                onBack={goToOptions}
            >
                {I18n.t('useApp.loading')}
            </Container>
        );
    }

    const showQr = qrRequested || !start.tiqrCookiePresent;

    const suspended = poll?.status === 'SUSPENDED' ? poll : null;

    const suspendedAlert = suspended && (
        <Alert variant="warning">
            <WarningDiamondIcon />
            <AlertDescription>
                {I18n.t('useApp.suspended')}{' '}
                {suspended.suspendedMinutes > 0
                    ? I18n.t('useApp.suspendedWait', {
                          minutes: suspended.suspendedMinutes,
                          unit: I18n.t(
                              suspended.suspendedMinutes === 1
                                  ? 'useApp.minute'
                                  : 'useApp.minutes',
                          ),
                      })
                    : I18n.t('useApp.suspendedRetry')}
            </AlertDescription>
        </Alert>
    );

    if (!showQr) {
        return (
            <Container
                title={I18n.t('useApp.title')}
                description={I18n.t('useApp.description')}
                email={email}
                onBack={goToOptions}
                footer={footer}
            >
                <div className={styles.help}>
                    <span>{I18n.t('useApp.noNotification')}</span>
                    <Button
                        variant="link"
                        type="button"
                        className={styles.link}
                        onClick={() => setQrRequested(true)}
                    >
                        {I18n.t('useApp.createQr')}
                    </Button>
                </div>
                {suspendedAlert}
            </Container>
        );
    }

    const qrImage = (
        <img
            className={styles.qr}
            src={start.qr}
            alt={I18n.t('useApp.qrAlt')}
            width={230}
            height={231}
        />
    );

    return (
        <Container
            title={I18n.t('useApp.scanTitle')}
            email={email}
            onBack={goToOptions}
            footer={footer}
        >
            {isTouchDevice() ? (
                <div className={styles.qrMobile}>
                    <a href={start.url}>{qrImage}</a>
                    <Button
                        type="button"
                        onClick={() => (window.location.href = start.url)}
                    >
                        {I18n.t('useApp.openApp')}
                    </Button>
                </div>
            ) : (
                <div className={styles.qrWrapper}>{qrImage}</div>
            )}

            <div className={styles.help}>
                <span>{I18n.t('useApp.offline')}</span>
                <Button
                    variant="link"
                    type="button"
                    className={styles.link}
                    onClick={() => setCodeRequested(true)}
                >
                    {I18n.t('useApp.enterCode')}
                </Button>
            </div>

            {codeRequested && (
                <div className={styles.code}>
                    <OtpInput
                        ariaLabel={I18n.t('useApp.codeLabel')}
                        value={code}
                        onChange={setCode}
                        disabled={isVerifying}
                        onComplete={(value) => submitCode(value.trim())}
                    />
                    {codeFailed && (
                        <Alert variant="warning">
                            <WarningDiamondIcon />
                            <AlertDescription>
                                {I18n.t('useApp.codeIncorrect')}
                            </AlertDescription>
                        </Alert>
                    )}
                </div>
            )}
            {suspendedAlert}
        </Container>
    );
}
