import {
    type FC,
    type ReactNode,
    type SubmitEvent,
    useEffect,
    useRef,
    useState,
} from 'react';
import { useLocation, useNavigate, useParams } from 'react-router';

import { OtpInput } from '@myconext/shared-gui';
import { Button } from '@surfnet/curve-react';
import { useMutation } from '@tanstack/react-query';

import { generateCodeRequest, verifyCodeRequest } from '../../api';
import {
    LoadingButton,
    LoadingSpinner,
    LoginContainer,
} from '../../components';
import I18n from '../../locale/I18n';
import { rememberLoginMethod } from '../../utils/loginMethods';
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
    const codeSent: boolean = state?.codeSent === true;

    const [code, setCode] = useState('');
    const hasFired = useRef(false);

    const goToLogin = () => navigate(`/login/${id}`);
    const goToOptions = () => navigate(`/options/${id}`, { state: { email } });

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
        if (!codeSent) {
            generateCode();
        }
    }, []);

    const {
        mutate: submitCode,
        isPending: isVerifying,
        isError: verifyFailed,
    } = useMutation({
        mutationFn: (code: string) => verifyCodeRequest(code, id),
        onSuccess: ({ url }) => {
            rememberLoginMethod('useCode');
            window.location.href = url;
        },
    });

    const isValidForm = code.trim() !== '';

    const handleSubmit = (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (isVerifying || !isValidForm) {
            return;
        }
        submitCode(code.trim());
    };

    const footer = (
        <>
            <p>{I18n.t('codeScreen.noAccess')}</p>
            <Button
                variant="link"
                className={styles.link}
                onClick={goToOptions}
            >
                {I18n.t('codeScreen.otherMethod')}
            </Button>
        </>
    );

    if (isGenerating) {
        return (
            <Container email={email} onBack={goToLogin}>
                <LoadingSpinner label={I18n.t('codeScreen.sending')} />
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
            <form className={styles.form} onSubmit={handleSubmit}>
                <OtpInput
                    ariaLabel={I18n.t('codeScreen.inputLabel')}
                    value={code}
                    onChange={setCode}
                    disabled={isVerifying}
                    onComplete={(value) => submitCode(value.trim())}
                />
                {verifyFailed && (
                    <p role="alert">Invalid code, please try again.</p>
                )}

                <LoadingButton
                    type="submit"
                    className={styles.submit}
                    loading={isVerifying}
                    disabled={!isValidForm}
                >
                    {I18n.t('codeScreen.submit')}
                </LoadingButton>
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
