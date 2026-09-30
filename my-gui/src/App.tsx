import { NavLink } from 'react-router';

import { Button } from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import './App.scss';
import { fetchConfig } from './api';
import { useAppStore } from './store/store';

function App() {
    const dark = useAppStore((state) => state.dark);
    const toggleDark = useAppStore((state) => state.toggleDark);

    const {
        data: config,
        isLoading,
        isError,
    } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });

    if (isLoading) {
        return <div>Loading...</div>;
    }

    if (isError) {
        return <div>Error!</div>;
    }

    return (
        <>
            <h1>Hello My-GUI!</h1>
            <Button onClick={toggleDark}>
                {dark ? '☀️ Light mode' : '🌙 Dark mode'}
            </Button>
            <NavLink to="/about">[ABOUT]</NavLink>
            <NavLink to="/security">[Security (Authenticated only)]</NavLink>
            <pre>{JSON.stringify(config, null, 2)}</pre>
        </>
    );
}

export default App;
