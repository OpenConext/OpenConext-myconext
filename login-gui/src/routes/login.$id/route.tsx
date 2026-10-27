import { type FC, type ReactNode, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';

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

import { fetchLoginMethods, fetchServiceName } from '../../api';
import { LoginContainer } from '../../components';
import I18n from '../../locale/I18n';
import styles from './route.module.scss';

const Footer = () => {
    const { id } = useParams();

    return (
        <>
            <p>{I18n.t('login.noAccount')}</p>
            <Link to={`/request/${id}`}>{I18n.t('login.createAccount')}</Link>
        </>
    );
};

const Container: FC<{
    children: ReactNode;
    serviceName?: string;
}> = ({ children, serviceName }) => (
    <LoginContainer
        title={I18n.t('login.title')}
        description={
            serviceName
                ? I18n.t('login.descriptionWithService', { serviceName })
                : I18n.t('login.description')
        }
        footer={<Footer />}
    >
        {children}
    </LoginContainer>
);

export default function Login() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [email, setEmail] = useState('');
    const [accountNotFound, setAccountNotFound] = useState(false);

    const { data: service, isLoading } = useQuery({
        queryKey: ['serviceName', id],
        queryFn: () => fetchServiceName(id!),
        enabled: Boolean(id),
    });

    const { mutate: submitEmail, isPending } = useMutation({
        mutationFn: (email: string) => fetchLoginMethods(email),
        onSuccess: (methods) => {
            if (methods.includes('useCode')) {
                navigate(`/code/${id}`, { state: { email } });
            }
        },
        onError: (error) => {
            if (error instanceof Response && error.status === 404) {
                setAccountNotFound(true);
            }
        },
    });

    if (isLoading || isPending) {
        return (
            <Container serviceName={service?.name}>
                {I18n.t('login.loading')}
            </Container>
        );
    }

    return (
        <Container serviceName={service?.name}>
            <form
                className={styles.form}
                onSubmit={(e) => {
                    e.preventDefault();
                    submitEmail(email);
                }}
            >
                <Field>
                    <FieldLabel htmlFor="email">
                        {I18n.t('login.email')}
                    </FieldLabel>
                    <Input
                        id="email"
                        type="email"
                        value={email}
                        onChange={(e) => {
                            setEmail(e.target.value);
                            setAccountNotFound(false);
                        }}
                    />
                </Field>
                {accountNotFound && (
                    <Alert variant="warning">
                        <WarningDiamondIcon />
                        <AlertDescription>
                            {I18n.t('login.accountNotFound')}
                        </AlertDescription>
                    </Alert>
                )}
                <Button type="submit">{I18n.t('login.submit')}</Button>
            </form>
        </Container>
    );
}
