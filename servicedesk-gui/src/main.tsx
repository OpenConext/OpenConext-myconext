import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import 'react-tooltip/dist/react-tooltip.css';

import { EnvironmentRibbon } from '@myconext/shared-gui';
import { Toaster } from '@surfnet/curve-react';
import '@surfnet/curve-react/styles.css';

import App from './App';
import './index.scss';

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <EnvironmentRibbon />
        <BrowserRouter>
            <Routes>
                <Route path="/*" element={<App />} />
            </Routes>
        </BrowserRouter>
        <Toaster position="bottom-right" offset={35} mobileOffset={35} />
    </StrictMode>,
);
