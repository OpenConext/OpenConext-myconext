import { type FC, type ReactNode, useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router';

import { UserCircleIcon, UserSwitchIcon } from '@phosphor-icons/react';
import { Field, FieldLabel, Input } from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import { fetchLoginMethods, fetchServiceName } from '../../api';
import {
    ErrorAlert,
    ItemList,
    LoadingButton,
    LoadingPage,
    LoginContainer,
    Spinner,
} from '../../components';
import I18n from '../../locale/I18n';
import {
    forgetKnownUser,
    getKnownUser,
    loginMethodPath,
    pickLoginMethod,
    rememberKnownUser,
} from '../../utils/loginMethods';
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
    const { id = '' } = useParams();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const [knownUser, setKnownUser] = useState(getKnownUser);
    const [email, setEmail] = useState('');
    const [accountNotFound, setAccountNotFound] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const emailInputRef = useRef<HTMLInputElement>(null);

    // A disabled input loses focus, so restore it once submitting ends.
    useEffect(() => {
        if (!isSubmitting) {
            emailInputRef.current?.focus();
        }
    }, [isSubmitting]);

    const { data: service, isLoading } = useQuery({
        queryKey: ['serviceName', id],
        queryFn: () => fetchServiceName(id),
        enabled: Boolean(id),
    });

    const { mutate } = useMutation({
        mutationFn: (email: string) => fetchLoginMethods(email),
        onSuccess: (methods, email) => {
            rememberKnownUser(email);
            const method = pickLoginMethod(
                methods,
                searchParams.has('magicLink'),
                searchParams.has('mfa'),
            );

            if (method) {
                navigate(loginMethodPath(method, id), { state: { email } });
            } else {
                setIsSubmitting(false);
            }
        },
        onError: (error, submittedEmail) => {
            setIsSubmitting(false);
            if (error instanceof Response && error.status === 404) {
                // A stale remembered account would otherwise dead-end the user.
                if (submittedEmail === knownUser) {
                    forgetKnownUser();
                    setKnownUser(undefined);
                }
                setAccountNotFound(true);
            }
        },
    });

    const handleSubmitEmail = (value: string) => {
        setIsSubmitting(true);
        mutate(value);
    };

    if (isLoading) {
        return <LoadingPage />;
    }

    if (knownUser) {
        return (
            <Container serviceName={service?.name}>
                <ItemList
                    items={[
                        {
                            key: 'known-user',
                            icon: UserCircleIcon,
                            label: knownUser,
                            onClick: () => {
                                if (!isSubmitting) {
                                    handleSubmitEmail(knownUser);
                                }
                            },
                        },
                        {
                            key: 'other-account',
                            icon: UserSwitchIcon,
                            label: I18n.t('login.useOtherAccount'),
                            onClick: () => {
                                if (isSubmitting) {
                                    return;
                                }
                                forgetKnownUser();
                                setKnownUser(undefined);
                                setAccountNotFound(false);
                            },
                        },
                    ]}
                />
                {isSubmitting && <Spinner />}
                {accountNotFound && (
                    <ErrorAlert>{I18n.t('login.accountNotFound')}</ErrorAlert>
                )}
            </Container>
        );
    }

    return (
        <Container serviceName={service?.name}>
            <form
                className={styles.form}
                onSubmit={(e) => {
                    e.preventDefault();
                    // Guards Enter-key submits while a request is in flight.
                    if (!isSubmitting) {
                        handleSubmitEmail(email);
                    }
                }}
            >
                <Field>
                    <FieldLabel htmlFor="email">
                        {I18n.t('login.email')}
                    </FieldLabel>
                    <Input
                        id="email"
                        ref={emailInputRef}
                        type="email"
                        autoFocus
                        value={email}
                        disabled={isSubmitting}
                        onChange={(e) => {
                            setEmail(e.target.value);
                            setAccountNotFound(false);
                        }}
                    />
                </Field>
                {accountNotFound && (
                    <ErrorAlert>{I18n.t('login.accountNotFound')}</ErrorAlert>
                )}
                <LoadingButton type="submit" loading={isSubmitting}>
                    {I18n.t('login.submit')}
                </LoadingButton>
            </form>
        </Container>
    );
}
