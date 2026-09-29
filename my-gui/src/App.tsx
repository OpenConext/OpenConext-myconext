import { NavLink } from 'react-router';

import { Button } from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import './App.scss';
import { fetchConfig } from './api';
import { useDarkModeStore } from './store/store';

function App() {
    const dark = useDarkModeStore((state) => state.dark);
    const toggleDark = useDarkModeStore((state) => state.toggleDark);

    const {
        data: config,
        isLoading,
        isError,
    } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });

    if (isLoading) return <div>Loading...</div>;
    if (isError) return <div>Error!</div>;

    return (
        <>
            <h1>Hello My-GUI!</h1>
            <Button onClick={toggleDark}>
                {dark ? '☀️ Light mode' : '🌙 Dark mode'}
            </Button>
            <NavLink to="/about">ABOUT</NavLink>
            <pre>{JSON.stringify(config, null, 2)}</pre>
        </>
    );
}

export default App;
