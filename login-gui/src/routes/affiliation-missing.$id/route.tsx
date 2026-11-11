import { useParams } from 'react-router';

import { LinkOutcome } from '../../components';

export default function AffiliationMissing() {
    const { id = '' } = useParams();

    return (
        <LinkOutcome
            id={id}
            scope="affiliationMissing"
            retryTarget="institution"
        />
    );
}
