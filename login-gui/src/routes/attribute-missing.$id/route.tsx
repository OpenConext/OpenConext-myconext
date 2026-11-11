import { useParams } from 'react-router';

import { LinkOutcome } from '../../components';

export default function AttributeMissing() {
    const { id = '' } = useParams();

    return (
        <LinkOutcome
            id={id}
            scope="attributeMissing"
            retryTarget="institution"
        />
    );
}
