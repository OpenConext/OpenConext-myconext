import React, {useEffect, useState} from 'react'
import {Loader} from "@surfnet/sds";
import './App.scss';
import {Navigate, Route, Routes, useNavigate} from "react-router-dom";
// import {useNavigate} from "react-router-dom";
import {configuration} from "./api/index.js";
import {useAppStore} from "./stores/AppStore.js";
import {Flash} from "./components/Flash.jsx";
import {Header} from "./components/Header.jsx";
import {BreadCrumb} from "./components/BreadCrumb.jsx";
import NotFound from "./pages/NotFound.jsx";
import RefreshRoute from "./pages/RefreshRoute.jsx";
import {Login} from "./pages/Login.jsx";
import {Home} from "./pages/Home.jsx";
import {Footer} from "./components/Footer.jsx";

const App = () => {

    const [loading, setLoading] = useState(true);
    const [isAuthenticated, setIsAuthenticated] = useState(false);

    const navigate = useNavigate();

    useEffect(() => {
        configuration().then(res => {
            useAppStore.setState(() => ({config: res, authenticated: res.authenticated, user: res.user}));
            setLoading(false);
            setIsAuthenticated(res.authenticated);
            if (res.authenticated && res.user?.serviceDeskMember) {
                navigate("/home")
            } else if (res.authenticated && !res.user)
                navigate("/not-found")
            else {
                navigate("/login")
            }
        });

    }, []);

    if (loading) {
        return <Loader/>
    }

    return (
        <div className="service-desk">
            <div className="container">
                <Flash/>
                <Header/>
                {isAuthenticated && <BreadCrumb/>}
                {isAuthenticated &&
                    <Routes>
                        <Route path="/" element={<Navigate replace to="home"/>}/>
                        <Route path="home/:tab?" element={<Home/>}/>
                        <Route path="login" element={<Login/>}/>
                        <Route path="refresh-route/:path" element={<RefreshRoute/>}/>
                        <Route path="*" element={<NotFound/>}/>
                    </Routes>}
                {!isAuthenticated &&
                    <Routes>
                        <Route path="/" element={<Navigate replace to="login"/>}/>
                        <Route path="/home" element={<Navigate replace to="login"/>}/>
                        <Route path="login" element={<Login/>}/>
                        <Route path="/*" element={<NotFound/>}/>
                    </Routes>}
            </div>
            {<Footer/>}
        </div>
    );
}

export default App;