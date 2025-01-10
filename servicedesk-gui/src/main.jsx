import {createRoot} from 'react-dom/client'
import App from './App.jsx'
import {BrowserRouter, Route, Routes} from "react-router-dom";
//Do not change the order of @surfnet.sds style imports
import '@surfnet/sds/styles/sds.css';
import '@surfnet/sds/cjs/index.css';
import "react-tooltip/dist/react-tooltip.css";
//Always keep these two last
import './index.scss';


createRoot(document.getElementById('root')).render(
    <BrowserRouter>
        <Routes>
            <Route path="/*" element={<App/>}/>
        </Routes>
    </BrowserRouter>
);