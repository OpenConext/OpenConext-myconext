import { Navigate, useLocation } from 'react-router';

// the server redirects here after linking, an institution belongs on the
// institutions page
export default function PersonalRedirect() {
    const { search } = useLocation();
    const institution = new URLSearchParams(search).has('institution');
    return (
        <Navigate
            to={{
                pathname: institution ? '/institutions' : '/identity',
                search,
            }}
            replace
        />
    );
}
