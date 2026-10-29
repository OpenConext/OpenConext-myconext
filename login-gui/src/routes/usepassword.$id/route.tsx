import { type FC, type ReactNode, useState } from 'react';
import { Navigate, useLocation, useNavigate, useParams } from 'react-router';

import { WarningDiamondIcon } from '@phosphor-icons/react';
import {
    Alert,
    AlertDescription,
    Button,
    Field,
    FieldLabel,
    Input,
} from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import { fetchServiceName, passwordLoginRequest } from '../../api';
import { LoginContainer } from '../../components';
import I18n from '../../locale/I18n';
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

    const { data: service } = useQuery({
        queryKey: ['serviceName', id],
        queryFn: () => fetchServiceName(id),
        enabled: id !== '',
    });

    const {
        mutate: submitPassword,
        isPending,
        isError,
    } = useMutation({
        mutationFn: (password: string) =>
            passwordLoginRequest(email, password, id),
        onSuccess: ({ url }) => {
            if (url) {
                window.location.href = url;
            }
        },
        onError: (error) => {
            if (error instanceof Response && error.status === 403) {
                setIncorrect(true);
                setPassword('');
            }
        },
    });

    if (!email) {
        return <Navigate to={`/login/${id}`} replace />;
    }

    const goToOptions = () => navigate(`/options/${id}`, { state: { email } });

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
            <form
                className={styles.form}
                onSubmit={(e) => {
                    e.preventDefault();
                    setIncorrect(false);
                    submitPassword(password);
                }}
            >
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
                        type="password"
                        autoComplete="current-password"
                        autoFocus
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </Field>
                {incorrect && (
                    <Alert variant="warning">
                        <WarningDiamondIcon />
                        <AlertDescription>
                            {I18n.t('password.incorrect')}
                        </AlertDescription>
                    </Alert>
                )}
                {isError && !incorrect && (
                    <Alert variant="warning">
                        <WarningDiamondIcon />
                        <AlertDescription>
                            {I18n.t('password.error')}
                        </AlertDescription>
                    </Alert>
                )}
                <Button type="submit" disabled={isPending || password === ''}>
                    {I18n.t('password.submit')}
                </Button>
            </form>
        </Container>
    );
}
