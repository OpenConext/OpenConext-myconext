import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

import '@surfnet/curve-react/styles.css';

import App from './App.tsx';
import './index.scss';

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <App />
    </StrictMode>,
);
