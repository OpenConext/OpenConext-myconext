import { useParams } from 'react-router';

import { LinkOutcome } from '../../components';

export default function ValidNameMissing() {
    const { id = '' } = useParams();

    return (
        <LinkOutcome
            id={id}
            scope="validNameMissing"
            retryTarget="institution"
        />
    );
}
