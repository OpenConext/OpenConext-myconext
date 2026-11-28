import { useEffect } from 'react';
import { Navigate, Route, Routes, useNavigate } from 'react-router-dom';

import { Spinner } from '@surfnet/curve-react';

import './App.scss';
import { Flash } from './components/Flash';
import { Footer } from './components/Footer';
import { TopNavigation } from './components/TopNavigation';
import Forbidden from './pages/Forbidden';
import IdentityCheck from './pages/IdentityCheck';
import { Login } from './pages/Login';
import NotFound from './pages/NotFound';
import { Overview } from './pages/Overview';
import RefreshRoute from './pages/RefreshRoute';
import { Stats } from './pages/Stats';
import { UserDetail } from './pages/UserDetail';
import { Users } from './pages/Users';
import { AUTH_STATUS, useAppStore } from './stores/AppStore';

const isLoginPath = () => window.location.pathname.endsWith('/login');

const App = () => {
    const storeAuthStatus = useAppStore((state) => state.authStatus);
    const loadSession = useAppStore((state) => state.loadSession);
    const navigate = useNavigate();
    // On the login page there is no session to load yet
    const authStatus =
        storeAuthStatus === AUTH_STATUS.LOADING && isLoginPath()
            ? AUTH_STATUS.UNAUTHENTICATED
            : storeAuthStatus;

    // useNavigate returns a new function on every location change, so this
    // effect re-runs on each tab switch; loadSession only fetches the first time
    useEffect(() => {
        if (isLoginPath()) {
            return;
        }
        const load = async () => {
            try {
                await loadSession();
            } catch {
                // The failure is cached, don't redirect again once we're there
                if (!window.location.pathname.endsWith('/forbidden')) {
                    setTimeout(
                        () => navigate('/forbidden?unauthorized=true'),
                        125,
                    );
                }
            }
        };
        void load();
    }, [loadSession, navigate]);

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
                            element={<Navigate replace to="/overview" />}
                        />
                        <Route path="/overview" element={<Overview />} />
                        <Route path="/users" element={<Users />} />
                        <Route path="/users/:id" element={<UserDetail />} />
                        <Route
                            path="/identity-check"
                            element={<IdentityCheck />}
                        />
                        <Route path="/stats" element={<Stats />} />
                        <Route
                            path="/home/*"
                            element={<Navigate replace to="/overview" />}
                        />
                        <Route
                            path="/refresh-route/:path"
                            element={<RefreshRoute />}
                        />
                        <Route path="/forbidden" element={<Forbidden />} />
                        <Route
                            path="/login"
                            element={<Navigate replace to="/overview" />}
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
