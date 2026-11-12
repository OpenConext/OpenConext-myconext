import { useEffect, useState } from 'react';

import { sanitizeHtml } from '@myconext/shared-gui';
import { Button } from '@surfnet/curve-react';

import { configuration } from '../api';
import { LandingInfo } from '../components/LandingInfo';
import FrontDesk from '../icons/frontdesk.svg';
import I18n from '../locale/I18n';
import type { Config } from '../stores/AppStore';
import './Login.scss';

export const Login = () => {
    const [isAnimating, setIsAnimating] = useState(false);
    const [config, setConfig] = useState<Config | null>(null);

    useEffect(() => {
        const load = async () => setConfig(await configuration());
        void load();
    }, []);

    const toggleAnimation = () => {
        setIsAnimating(true);
        setTimeout(() => setIsAnimating(false), 2250);
    };

    const doLogin = () => {
        if (!config?.loginUrlServiceDesk) {
            return;
        }
        const loginUrl = new URL(config.loginUrlServiceDesk);
        loginUrl.searchParams.set('redirect_path', '/');
        loginUrl.searchParams.set('registration_id', 'service_desk');
        loginUrl.searchParams.set('force', 'true');
        window.location.href = loginUrl.toString();
    };

    return (
        <div className="top-container">
            <div className="mod-login-container">
                <div className="mod-login">
                    <div className="header-left">
                        <h1
                            className="header-title"
                            dangerouslySetInnerHTML={{
                                __html: sanitizeHtml(
                                    I18n.t('landing.header.title'),
                                ),
                            }}
                        />
                        <Button
                            onClick={doLogin}
                            size="lg"
                            className="login-button"
                        >
                            {I18n.t('landing.header.login')}
                        </Button>
                        <p
                            className="sup"
                            dangerouslySetInnerHTML={{
                                __html: sanitizeHtml(
                                    I18n.t('landing.header.sup'),
                                ),
                            }}
                        />
                    </div>
                    <div
                        className={`header-right ${isAnimating ? 'animated' : ''}`}
                        onClick={toggleAnimation}
                    >
                        <FrontDesk />
                    </div>
                </div>
            </div>
            <LandingInfo />
        </div>
    );
};
