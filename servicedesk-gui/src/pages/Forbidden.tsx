import { Button } from '@surfnet/curve-react';

import { logout } from '../api';
import WarningLogo from '../icons/undraw_warning_qn4r.svg';
import I18n from '../locale/I18n';
import { useAppStore } from '../stores/AppStore';
import './Forbidden.scss';

const Forbidden = () => {
    const logoutUser = async () => {
        const { config } = useAppStore.getState();
        await logout();
        useAppStore.setState(() => ({
            user: {},
            controlCode: {},
        }));
        const param = encodeURIComponent('logout=true&delete=true');
        window.location.href = `${config.accountBaseUrl}/doLogout?param=${param}&app=servicedesk`;
    };

    return (
        <div className="forbidden-container">
            <div className="forbidden">
                <p>{I18n.t('forbidden.info')}</p>
                <WarningLogo />
                <div className="actions">
                    <Button onClick={() => void logoutUser()}>
                        {I18n.t('forbidden.logout')}
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default Forbidden;
