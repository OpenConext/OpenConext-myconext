import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import './index.scss'
import App from './App.jsx'
//Do not change the order of @surfnet.sds style imports
import '@surfnet/sds/styles/sds.css';
import '@surfnet/sds/cjs/index.css';
import {BrowserRouter, Route, Routes} from "react-router";

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <BrowserRouter>
            <Routes>
                <Route path="/*" element={<App/>}/>
            </Routes>
        </BrowserRouter>
    </StrictMode>
)
