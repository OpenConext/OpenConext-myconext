import { type FC, type ReactNode, useState } from 'react';
import { useNavigate, useParams } from 'react-router';

import { Button, Input } from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import { fetchLoginMethods, fetchServiceName } from '../../api';
import { LoginContainer } from '../../components';

const Footer = () => (
    <>
        <p>Don't have an eduID yet?</p>
        {/* TODO: link to the registration route once it exists */}
        <a href="#">Create an eduID</a>
    </>
);

const Container: FC<{ children: ReactNode; serviceName?: string }> = ({
    children,
    serviceName,
}) => (
    <LoginContainer
        title="Log in with eduID"
        description={`to continue to ${serviceName ?? ''}`}
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

    const {
        data: service,
        isLoading,
        isError,
    } = useQuery({
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
        return <Container serviceName={service?.name}>Loading...</Container>;
    }

    if (isError) {
        return (
            <Container serviceName={service?.name}>
                Error loading service name
            </Container>
        );
    }

    return (
        <Container serviceName={service?.name}>
            <Input
                type="email"
                value={email}
                onChange={(e) => {
                    setEmail(e.target.value);
                    setAccountNotFound(false);
                }}
            />
            {accountNotFound && (
                <p>Account does not exist, create new account?</p>
            )}
            <Button onClick={() => submitEmail(email)}>Continue</Button>
        </Container>
    );
}
