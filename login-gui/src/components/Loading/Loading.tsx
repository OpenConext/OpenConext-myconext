import { LoginContainer } from '../LoginContainer/LoginContainer';
import { LoadingSpinner } from './LoadingSpinner';

interface LoadingProps {
    title: string;
}

export const Loading = ({ title }: LoadingProps) => (
    <LoginContainer title={title}>
        <LoadingSpinner />
    </LoginContainer>
);
