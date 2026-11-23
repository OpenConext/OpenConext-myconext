import { Navigate, useLocation, useParams } from 'react-router';

import { UseAppFlow } from './UseAppFlow';

export default function UseApp() {
    const { id = '' } = useParams();
    const { state } = useLocation();
    const email = typeof state?.email === 'string' ? state.email : '';

    if (!email) {
        return <Navigate to={`/login/${id}`} replace />;
    }

    return <UseAppFlow email={email} id={id} />;
}
