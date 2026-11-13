import { LoginContainer } from '../LoginContainer/LoginContainer';
import { Spinner } from './Spinner.tsx';

interface LoadingProps {
    title: string;
}

export const Loading = ({ title }: LoadingProps) => (
    <LoginContainer title={title}>
        <Spinner />
    </LoginContainer>
);
