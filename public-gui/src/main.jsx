import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
//Do not change the order of @surfnet.sds style imports
import '@surfnet/sds/styles/sds.css';
import '@surfnet/sds/cjs/index.css';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
