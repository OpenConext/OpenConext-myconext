import { useParams, useSearchParams } from 'react-router';

import { LinkOutcome } from '../../components';

export default function SubjectAlreadyLinked() {
    const { id = '' } = useParams();
    const [searchParams] = useSearchParams();

    return (
        <LinkOutcome
            id={id}
            scope="subjectAlreadyLinked"
            options={{ email: searchParams.get('email') ?? '' }}
            retryTarget="stepup"
        />
    );
}
