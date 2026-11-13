import { useEffect, useState } from 'react';
import { Navigate, Route, Routes, useNavigate } from 'react-router-dom';

import { Spinner } from '@surfnet/curve-react';

import './App.scss';
import { configuration, me } from './api';
import { Flash } from './components/Flash';
import { Footer } from './components/Footer';
import { TopNavigation } from './components/TopNavigation';
import Forbidden from './pages/Forbidden';
import IdentityCheck from './pages/IdentityCheck';
import { Login } from './pages/Login';
import NotFound from './pages/NotFound';
import Placeholder from './pages/Placeholder';
import RefreshRoute from './pages/RefreshRoute';
import { UserDetail } from './pages/UserDetail';
import { Users } from './pages/Users';
import { useAppStore } from './stores/AppStore';

const AUTH_STATUS = {
    LOADING: 'loading',
    UNAUTHENTICATED: 'unauthenticated',
    FORBIDDEN: 'forbidden',
    AUTHORIZED: 'authorized',
} as const;

type AuthStatus = (typeof AUTH_STATUS)[keyof typeof AUTH_STATUS];

const App = () => {
    const [authStatus, setAuthStatus] = useState<AuthStatus>(
        AUTH_STATUS.LOADING,
    );
    const navigate = useNavigate();

    useEffect(() => {
        const load = async () => {
            if (window.location.pathname.endsWith('/login')) {
                setAuthStatus(AUTH_STATUS.UNAUTHENTICATED);
                return;
            }
            try {
                const config = await configuration();
                useAppStore.setState(() => ({ config }));
                if (!config.isAuthenticated) {
                    setAuthStatus(AUTH_STATUS.UNAUTHENTICATED);
                    return;
                }
                const user = await me();
                useAppStore.setState(() => ({ user }));
                setAuthStatus(
                    user.serviceDeskMember
                        ? AUTH_STATUS.AUTHORIZED
                        : AUTH_STATUS.FORBIDDEN,
                );
            } catch {
                setAuthStatus(AUTH_STATUS.FORBIDDEN);
                setTimeout(() => navigate('/forbidden?unauthorized=true'), 125);
            }
        };
        void load();
    }, [navigate]);

    if (authStatus === AUTH_STATUS.LOADING) {
        return <Spinner className="loading" />;
    }

    return (
        <div className="service-desk">
            <div className="container">
                <Flash />
                <TopNavigation />
                {authStatus === AUTH_STATUS.AUTHORIZED && (
                    <Routes>
                        <Route
                            path="/"
                            element={<Navigate replace to="/identity-check" />}
                        />
                        <Route
                            path="/overview"
                            element={
                                <Placeholder titleKey="navigation.overview" />
                            }
                        />
                        <Route path="/users" element={<Users />} />
                        <Route path="/users/:id" element={<UserDetail />} />
                        <Route
                            path="/identity-check"
                            element={<IdentityCheck />}
                        />
                        <Route
                            path="/home/*"
                            element={<Navigate replace to="/identity-check" />}
                        />
                        <Route
                            path="/refresh-route/:path"
                            element={<RefreshRoute />}
                        />
                        <Route path="/forbidden" element={<Forbidden />} />
                        <Route
                            path="/login"
                            element={<Navigate replace to="/identity-check" />}
                        />
                        <Route path="*" element={<NotFound />} />
                    </Routes>
                )}
                {authStatus === AUTH_STATUS.FORBIDDEN && (
                    <Routes>
                        <Route path="/forbidden" element={<Forbidden />} />
                        <Route
                            path="*"
                            element={<Navigate replace to="/forbidden" />}
                        />
                    </Routes>
                )}
                {authStatus === AUTH_STATUS.UNAUTHENTICATED && (
                    <Routes>
                        <Route path="/login" element={<Login />} />
                        <Route
                            path="*"
                            element={<Navigate replace to="/login" />}
                        />
                    </Routes>
                )}
            </div>
            <Footer />
        </div>
    );
};

export default App;
