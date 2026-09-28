import { useEffect, useState } from 'react';

import { Button } from '@surfnet/curve-react';

import './App.scss';

function App() {
    const [dark, setDark] = useState(false);

    useEffect(() => {
        document.documentElement.classList.toggle('dark', dark);
    }, [dark]);

    return (
        <>
            <h1>Hello My-GUI!</h1>
            <Button onClick={() => setDark((d) => !d)}>
                {dark ? '☀️ Light mode' : '🌙 Dark mode'}
            </Button>
        </>
    );
}

export default App;
