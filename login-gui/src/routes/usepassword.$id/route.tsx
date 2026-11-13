import {
    type FC,
    type ReactNode,
    type SubmitEvent,
    useEffect,
    useRef,
    useState,
} from 'react';
import { Navigate, useLocation, useNavigate, useParams } from 'react-router';

import { Button, Field, FieldLabel, Input } from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import { fetchServiceName, passwordLoginRequest } from '../../api';
import { ErrorAlert, LoadingButton, LoginContainer } from '../../components';
import I18n from '../../locale/I18n';
import { rememberLoginMethod } from '../../utils/loginMethods';
import styles from './route.module.scss';

const Container: FC<{
    children: ReactNode;
    email: string;
    serviceName?: string;
    onBack: () => void;
    footer?: ReactNode;
}> = ({ children, email, serviceName, onBack, footer }) => (
    <LoginContainer
        title={I18n.t('password.title')}
        description={
            serviceName
                ? I18n.t('password.descriptionWithService', { serviceName })
                : I18n.t('password.description')
        }
        footer={footer}
        onBack={onBack}
        backLabel={email}
    >
        {children}
    </LoginContainer>
);

export default function UsePassword() {
    const { id = '' } = useParams();
    const { state } = useLocation();
    const navigate = useNavigate();
    const email: string = state?.email ?? '';

    const [password, setPassword] = useState('');
    const [incorrect, setIncorrect] = useState(false);
    const passwordInputRef = useRef<HTMLInputElement>(null);

    const { data: service } = useQuery({
        queryKey: ['serviceName', id],
        queryFn: () => fetchServiceName(id),
        enabled: id !== '',
    });

    const {
        mutate: submitPassword,
        isPending,
        isSuccess,
        isError,
    } = useMutation({
        mutationFn: (password: string) =>
            passwordLoginRequest(email, password, id),
        onSuccess: ({ url }) => {
            rememberLoginMethod('usePassword');
            window.location.href = url;
        },
        onError: (error) => {
            if (error instanceof Response && error.status === 403) {
                setIncorrect(true);
                setPassword('');
            }
        },
    });

    const isSubmitting = isPending || isSuccess;

    useEffect(() => {
        if (!isSubmitting) {
            passwordInputRef.current?.focus();
        }
    }, [isSubmitting]);

    if (!email) {
        return <Navigate to={`/login/${id}`} replace />;
    }

    const goToOptions = () => navigate(`/options/${id}`, { state: { email } });

    const isValidForm = password !== '';

    const handleSubmit = (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (isSubmitting || !isValidForm) {
            return;
        }
        setIncorrect(false);
        submitPassword(password);
    };

    const footer = (
        <Button
            variant="link"
            type="button"
            className={styles.link}
            onClick={goToOptions}
        >
            {I18n.t('password.otherMethod')}
        </Button>
    );

    return (
        <Container
            email={email}
            serviceName={service?.name}
            onBack={goToOptions}
            footer={footer}
        >
            <form className={styles.form} onSubmit={handleSubmit}>
                <input
                    type="email"
                    hidden
                    readOnly
                    autoComplete="username"
                    value={email}
                />
                <Field>
                    <FieldLabel htmlFor="password">
                        {I18n.t('password.label')}
                    </FieldLabel>
                    <Input
                        id="password"
                        ref={passwordInputRef}
                        type="password"
                        autoComplete="current-password"
                        autoFocus
                        value={password}
                        disabled={isSubmitting}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </Field>
                {incorrect && (
                    <ErrorAlert>{I18n.t('password.incorrect')}</ErrorAlert>
                )}
                {isError && !incorrect && (
                    <ErrorAlert>{I18n.t('password.error')}</ErrorAlert>
                )}
                <LoadingButton
                    type="submit"
                    loading={isSubmitting}
                    disabled={!isValidForm}
                >
                    {I18n.t('password.submit')}
                </LoadingButton>
            </form>
        </Container>
    );
}
