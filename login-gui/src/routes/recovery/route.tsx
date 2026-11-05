import { Navigate, useNavigate, useSearchParams } from 'react-router';

import { RecoveryView } from '../../components';

export default function Recovery() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const hash = searchParams.get('h');

    if (!hash) {
        return <Navigate to="/expired" replace />;
    }

    return (
        <RecoveryView
            onBackupCode={() =>
                navigate(`/recovery-code?h=${encodeURIComponent(hash)}`)
            }
        />
    );
}
