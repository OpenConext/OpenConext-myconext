import { useSearchParams } from 'react-router';

export default function Landing() {
    const [searchParams] = useSearchParams();

    const logout = searchParams.get('logout');
    const deleted = searchParams.get('delete');
    const ratelimit = searchParams.get('ratelimit');

    if (logout) {
        return <h1>Placeholder: Logout</h1>;
    }

    if (deleted) {
        return <h1>Placeholder: Account deleted</h1>;
    }

    if (ratelimit) {
        return <h1>Placeholder: Rate limited</h1>;
    }

    return <h1>Placeholder: No parameters</h1>;
}
