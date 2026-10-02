import { useParams } from 'react-router';

import { useQuery } from '@tanstack/react-query';

import { fetchServiceName } from '../../api';

export default function Login() {
    const { id } = useParams();

    const {
        data: service,
        isLoading,
        isError,
    } = useQuery({
        queryKey: ['serviceName', id],
        queryFn: () => fetchServiceName(id!),
        enabled: Boolean(id),
    });

    // Entering email receive login methods (array) or a 404 error
    // ["useCode"]

    // For useCode:
    // Redirect frontend to /code/$id

    if (isLoading) {
        return <div>Loading...</div>;
    }

    if (isError) {
        return <div>Error loading service name</div>;
    }

    return <h1>{service?.name}</h1>;
}
