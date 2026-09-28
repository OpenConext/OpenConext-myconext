import { useEffect, useState } from 'react';

import { Button } from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import './App.scss';
import { fetchConfig } from './api';

function App() {
    const [dark, setDark] = useState(false);

    const {
        data: config,
        isLoading,
        isError,
    } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });

    useEffect(() => {
        document.documentElement.classList.toggle('dark', dark);
    }, [dark]);

    if (isLoading) return <div>Loading...</div>;
    if (isError) return <div>Error!</div>;

    return (
        <>
            <h1>Hello My-GUI!</h1>
            <Button onClick={() => setDark((d) => !d)}>
                {dark ? '☀️ Light mode' : '🌙 Dark mode'}
            </Button>
            <pre>{JSON.stringify(config, null, 2)}</pre>
        </>
    );
}

export default App;
