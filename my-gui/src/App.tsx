import { useQuery } from '@tanstack/react-query';

import './App.scss';
import { fetchConfig } from './api';

function App() {
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
            <pre>{JSON.stringify(config, null, 2)}</pre>
        </>
    );
}

export default App;
