import { LoginContainer } from '../LoginContainer/LoginContainer';
import { Spinner } from './Spinner.tsx';

interface LoadingProps {
    title: string;
}

export const LoadingLoginContainer = ({ title }: LoadingProps) => (
    <LoginContainer title={title}>
        <Spinner />
    </LoginContainer>
);
