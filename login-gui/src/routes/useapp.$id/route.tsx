import { Navigate, useLocation, useNavigate, useParams } from 'react-router';

import { useMutation } from '@tanstack/react-query';

import { tiqrManualResponse } from '../../api';
import { AppFlow, type ManualCode } from '../../components';
import { useTiqrAuthentication } from '../../hooks';

type UseAppFlowProps = {
    email: string;
    id: string;
};

const UseAppFlow = ({ email, id }: UseAppFlowProps) => {
    const navigate = useNavigate();
    const flow = useTiqrAuthentication(email, id);
    const sessionKey = flow.phase === 'active' ? flow.start.sessionKey : '';

    const {
        mutateAsync: submitCode,
        isPending: isVerifying,
        isError: codeFailed,
    } = useMutation({
        mutationFn: (code: string) => tiqrManualResponse(sessionKey, code),
    });

    const manualCode: ManualCode = {
        // On success the next poll returns SUCCESS and handles the redirect
        submit: async (code) => {
            try {
                await submitCode(code);
                return true;
            } catch {
                return false;
            }
        },
        isVerifying,
        failed: codeFailed,
    };

    const goToOptions = () => navigate(`/options/${id}`, { state: { email } });

    return (
        <AppFlow
            email={email}
            flow={flow}
            manualCode={manualCode}
            onOtherMethod={goToOptions}
        />
    );
};

export default function UseApp() {
    const { id = '' } = useParams();
    const { state } = useLocation();
    const email = typeof state?.email === 'string' ? state.email : '';

    if (!email) {
        return <Navigate to={`/login/${id}`} replace />;
    }

    return <UseAppFlow email={email} id={id} />;
}
