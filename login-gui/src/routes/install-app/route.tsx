import { redirectToAppStore } from '@myconext/shared-gui';

import { AppStores, LoginContainer } from '../../components';
import I18n from '../../locale/I18n';

// opened from the QR code on the get app step, sends the phone to its app store
// eslint-disable-next-line react-refresh/only-export-components
export function clientLoader() {
    redirectToAppStore({
        appleUrl: I18n.t('getApp.apple'),
        googleUrl: I18n.t('getApp.google'),
    });
    return null;
}

// public, everything but a phone gets the stores to choose from
export default function InstallApp() {
    return (
        <LoginContainer
            title={I18n.t('getApp.title')}
            description={I18n.t('getApp.description')}
            headerAlign="start"
        >
            <AppStores />
        </LoginContainer>
    );
}
