import { Navigate, useLocation } from 'react-router';

export default function SubjectAlreadyLinkedRedirect() {
    const { search } = useLocation();
    return (
        <Navigate
            to={{ pathname: '/identity/subject-already-linked', search }}
            replace
        />
    );
}
