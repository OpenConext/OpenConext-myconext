import { InstallAppQr, StoreBadges, isMobile } from '@myconext/shared-gui';

import I18n from '../../locale/I18n';
import styles from './AppEnrollment.module.scss';

export const AppStores = () => (
    <div className={styles.stores}>
        <StoreBadges
            googleUrl={I18n.t('getApp.google')}
            appleUrl={I18n.t('getApp.apple')}
            googleLabel={I18n.t('getApp.googleLabel')}
            appleLabel={I18n.t('getApp.appleLabel')}
        />
        {/* a phone cannot scan its own screen */}
        {!isMobile() && (
            <div className={styles.installQr}>
                <InstallAppQr title={I18n.t('getApp.qr')} />
            </div>
        )}
    </div>
);
