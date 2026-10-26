import { type FC, type ReactNode, useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router';

import {
    Button,
    InputOTP,
    InputOTPGroup,
    InputOTPSlot,
} from '@surfnet/curve-react';
import { useMutation } from '@tanstack/react-query';

import { generateCodeRequest, verifyCodeRequest } from '../../api';
import { LoginContainer } from '../../components';
import I18n from '../../locale/I18n';
import styles from './route.module.scss';

const Container: FC<{
    children: ReactNode;
    email: string;
    onBack: () => void;
    footer?: ReactNode;
}> = ({ children, email, onBack, footer }) => (
    <LoginContainer
        title={I18n.t('codeScreen.title')}
        description={I18n.t('codeScreen.description', { email })}
        footer={footer}
        onBack={onBack}
        backLabel={email}
    >
        {children}
    </LoginContainer>
);

export default function Code() {
    const { id = '' } = useParams();
    const { state } = useLocation();
    const navigate = useNavigate();
    const email: string = state?.email ?? '';

    const [code, setCode] = useState('');
    const hasFired = useRef(false);

    const goToLogin = () => navigate(`/login/${id}`);

    const {
        mutate: generateCode,
        isPending: isGenerating,
        isError: generateFailed,
    } = useMutation({
        mutationFn: () => generateCodeRequest(email, id),
    });

    useEffect(() => {
        if (hasFired.current) {
            return;
        }

        hasFired.current = true;
        generateCode();
    }, []);

    const {
        mutate: submitCode,
        isPending: isVerifying,
        isError: verifyFailed,
    } = useMutation({
        mutationFn: (code: string) => verifyCodeRequest(code, id),
        onSuccess: ({ url }) => (window.location.href = url),
    });

    const footer = (
        <>
            <p>{I18n.t('codeScreen.noAccess')}</p>
            <Button variant="link" className={styles.link} onClick={goToLogin}>
                {I18n.t('codeScreen.otherMethod')}
            </Button>
        </>
    );

    if (isGenerating) {
        return (
            <Container email={email} onBack={goToLogin}>
                Sending code...
            </Container>
        );
    }

    if (generateFailed) {
        return (
            <Container email={email} onBack={goToLogin}>
                Failed to send code. Please try again.
            </Container>
        );
    }

    return (
        <Container email={email} onBack={goToLogin} footer={footer}>
            <form
                className={styles.form}
                onSubmit={(e) => {
                    e.preventDefault();
                    submitCode(code.trim());
                }}
            >
                <InputOTP
                    containerClassName={styles.otp}
                    aria-label={I18n.t('codeScreen.inputLabel')}
                    maxLength={6}
                    value={code}
                    onChange={setCode}
                    disabled={isVerifying}
                    onComplete={(value: string) => submitCode(value.trim())}
                >
                    <InputOTPGroup>
                        <InputOTPSlot index={0} />
                        <InputOTPSlot index={1} />
                        <InputOTPSlot index={2} />
                        <InputOTPSlot index={3} />
                        <InputOTPSlot index={4} />
                        <InputOTPSlot index={5} />
                    </InputOTPGroup>
                </InputOTP>
                {verifyFailed && (
                    <p role="alert">Invalid code, please try again.</p>
                )}

                <Button
                    type="submit"
                    className={styles.submit}
                    disabled={isVerifying}
                >
                    {I18n.t('codeScreen.submit')}
                </Button>
            </form>

            <div className={styles.help}>
                <span>{I18n.t('codeScreen.noEmail')}</span>
                <Button
                    variant="link"
                    className={`${styles.link} ${styles.resend}`}
                    onClick={() => generateCode()}
                >
                    {I18n.t('codeScreen.resend')}
                </Button>
            </div>
        </Container>
    );
}
