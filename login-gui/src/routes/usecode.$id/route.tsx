import { Navigate, useParams } from 'react-router';

import { getKnownUser } from '../../utils/loginMethods';

export default function UseCode() {
    const { id = '' } = useParams();
    const email = getKnownUser();

    if (!email) {
        return <Navigate to={`/login/${id}`} replace />;
    }

    return <Navigate to={`/code/${id}`} state={{ email }} replace />;
}
