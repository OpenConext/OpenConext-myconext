import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { Spinner } from '@surfnet/curve-react';

const RefreshRoute = () => {
    const { path } = useParams();
    const navigate = useNavigate();

    useEffect(() => {
        void navigate(decodeURIComponent(path ?? '/'));
    }, [path, navigate]);

    return <Spinner className="loading" />;
};

export default RefreshRoute;
