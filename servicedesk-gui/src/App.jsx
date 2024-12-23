import {useEffect, useState} from 'react'
import reactLogo from './assets/react.svg'

import './App.scss'
import {useNavigate} from "react-router-dom";
import {useAppStore} from "./stores/AppStore.js";
import {configuration, csrf, me} from "./api/index.js";

export const App = () => {

    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();
    const {user} = useAppStore(state => state);

    useEffect(() => {
        setLoading(true);
        csrf().then(token => {
            useAppStore.setState(() => ({csrfToken: token.token}));
            configuration()
                .then(res => {
                    useAppStore.setState(() => ({config: res}));
                    if (!res.authenticated) {
                        if (!res.name) {
                            const direction = window.location.pathname + window.location.search;
                            localStorage.setItem("location", direction);
                        } else if (!isEmpty(res.missingAttributes)) {
                            setLoading(false);
                            navigate("/missingAttributes");
                            return;
                        }
                        const pathname = localStorage.getItem("location") || window.location.pathname;
                        const isInvitationAcceptFlow = window.location.pathname.startsWith("/invitation/accept");
                        if (res.name && !pathname.startsWith("/invitation/accept") && !isInvitationAcceptFlow) {
                            setLoading(false);
                            navigate("/deadend");
                        } else if (pathname === "/" || pathname.startsWith("/login") || pathname.startsWith("/invitation/accept") || isInvitationAcceptFlow) {
                            setLoading(false);
                            navigate(isInvitationAcceptFlow ? (window.location.pathname + window.location.search) : pathname);
                        } else {
                            //Bookmarked URL's trigger a direct login and skip the landing page
                            login(res);
                        }
                    } else {
                        me()
                            .then(res => {
                                useAppStore.setState(() => ({user: res, authenticated: true}));
                                setLoading(false);
                                const location = localStorage.getItem("location") || window.location.pathname + window.location.search;
                                const newLocation = location.startsWith("/login") ? "/home" : location;
                                localStorage.removeItem("location");
                                navigate(newLocation);
                            });
                    }
                })
                .catch(() => {
                    setLoading(false);
                    navigate("/deadend");
                })
        })
    }, [reload, impersonator]); // eslint-disable-line react-hooks/exhaustive-deps

    if (loading) {
        return <Loader/>
    }


    return (
        <>
            <span>TODO</span>
            <span>Routes -> with login</span>
        </>
    )
}
