import { useParams, useSearchParams } from 'react-router';

import { LinkOutcome } from '../../components';

export default function EppnAlreadyLinked() {
    const { id = '' } = useParams();
    const [searchParams] = useSearchParams();

    return (
        <LinkOutcome
            id={id}
            scope="eppnAlreadyLinked"
            options={{ email: searchParams.get('email') ?? '' }}
            retryTarget="institution"
        />
    );
}
