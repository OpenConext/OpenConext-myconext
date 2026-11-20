import { Navigate, useParams } from 'react-router';

import { getKnownUser } from '../../utils/loginMethods';

// Entry point of the remember-me redirect. The code route sends the email
// itself when it is not yet marked as sent, so this only hands over the email.
export default function UseCode() {
    const { id = '' } = useParams();
    const email = getKnownUser();

    if (!email) {
        return <Navigate to={`/login/${id}`} replace />;
    }

    return <Navigate to={`/code/${id}`} state={{ email }} replace />;
}
