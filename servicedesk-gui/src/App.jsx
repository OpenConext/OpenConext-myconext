import React, {useEffect, useState} from 'react'
import {Loader} from "@surfnet/sds";
import './App.scss';
import {Navigate, Route, Routes} from "react-router-dom";
import {configuration, me} from "./api/index.js";
import {useAppStore} from "./stores/AppStore.js";
import {Flash} from "./components/Flash.jsx";
import {Header} from "./components/Header.jsx";
import NotFound from "./pages/NotFound.jsx";
import RefreshRoute from "./pages/RefreshRoute.jsx";
import {Login} from "./pages/Login.jsx";
import {Home} from "./pages/Home.jsx";
import {Footer} from "./components/Footer.jsx";
import Forbidden from "./pages/Forbidden.jsx";
import {useNavigate} from "react-router-dom";

const AUTH_STATUS = {
    LOADING: 'loading',
    UNAUTHENTICATED: 'unauthenticated',
    FORBIDDEN: 'forbidden',
    AUTHORIZED: 'authorized',
};

const App = () => {

    const [authStatus, setAuthStatus] = useState(AUTH_STATUS.LOADING);
    const navigate = useNavigate();

    useEffect(() => {
        if (window.location.pathname.endsWith("/login")) {
            setAuthStatus(AUTH_STATUS.UNAUTHENTICATED);
            return;
        }
        configuration()
            .then(res => {
                useAppStore.setState(() => ({config: res}));
                if (!res.isAuthenticated) {
                    setAuthStatus(AUTH_STATUS.UNAUTHENTICATED);
                    return;
                }
                return me()
                    .then(user => {
                        useAppStore.setState(() => ({user}));
                        setAuthStatus(user.serviceDeskMember
                            ? AUTH_STATUS.AUTHORIZED
                            : AUTH_STATUS.FORBIDDEN);
                    });
            })
            .catch(() => {
                setAuthStatus(AUTH_STATUS.UNAUTHENTICATED);
                navigate("/login?unauthorized=true")
            });
    }, []);

    if (authStatus === AUTH_STATUS.LOADING) {
        return <Loader/>;
    }

    return (
        <div className="service-desk">
            <div className="container">
                <Flash/>
                <Header/>
                {authStatus === AUTH_STATUS.AUTHORIZED && (
                    <Routes>
                        <Route path="/" element={<Navigate replace to="/home"/>}/>
                        <Route path="/home/:tab?" element={<Home/>}/>
                        <Route path="/refresh-route/:path" element={<RefreshRoute/>}/>
                        <Route path="/forbidden" element={<Forbidden/>}/>
                        <Route path="/login" element={<Navigate replace to="/home"/>}/>
                        <Route path="*" element={<NotFound/>}/>
                    </Routes>
                )}
                {authStatus === AUTH_STATUS.FORBIDDEN && (
                    <Routes>
                        <Route path="/forbidden" element={<Forbidden/>}/>
                        <Route path="*" element={<Navigate replace to="/forbidden"/>}/>
                    </Routes>
                )}
                {authStatus === AUTH_STATUS.UNAUTHENTICATED && (
                    <Routes>
                        <Route path="/login" element={<Login/>}/>
                        <Route path="*" element={<Navigate replace to="/login"/>}/>
                    </Routes>
                )}
            </div>
            <Footer/>
        </div>
    );
}

export default App;
