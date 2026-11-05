import { Navigate, useNavigate, useSearchParams } from 'react-router';

import { GetAppView } from '../../components';

export default function GetApp() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const hash = searchParams.get('h');

    if (!hash) {
        return <Navigate to="/expired" replace />;
    }

    return (
        <GetAppView
            onBack={() => navigate(-1)}
            onNext={() => navigate(`/enrollapp?h=${encodeURIComponent(hash)}`)}
        />
    );
}
