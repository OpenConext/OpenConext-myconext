import { useParams, useSearchParams } from 'react-router';

import { LinkOutcome } from '../../components';
import I18n from '../../locale/I18n';

export default function IapAssuranceMissing() {
    const { id = '' } = useParams();
    const [searchParams] = useSearchParams();

    const level =
        searchParams.get('level') === 'medium' ? 'levelMedium' : 'levelHigh';

    return (
        <LinkOutcome
            id={id}
            scope="iapAssuranceMissing"
            options={{ level: I18n.t(`iapAssuranceMissing.${level}`) }}
            retryTarget="stepup"
        />
    );
}
