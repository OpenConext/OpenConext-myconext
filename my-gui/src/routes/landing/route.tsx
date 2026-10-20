import { useSearchParams } from 'react-router';

import I18n from '../../locale/I18n';

export default function Landing() {
    const [searchParams] = useSearchParams();

    const reason = ['logout', 'delete', 'ratelimit'].find((key) =>
        searchParams.get(key),
    );

    return <h1>{I18n.t(`landing.${reason ?? 'none'}`)}</h1>;
}
