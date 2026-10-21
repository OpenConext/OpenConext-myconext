import { QRCodeSVG } from 'qrcode.react';

import { GuestLayout } from '../../components/GuestLayout';
import { StoreBadges } from '../../components/StoreBadges';
import I18n from '../../locale/I18n';
import './route.scss';

// opened from the QR code in the activate app dialog, sends the phone to its app store
// eslint-disable-next-line react-refresh/only-export-components
export function clientLoader() {
    const userAgent = navigator.userAgent;
    if (/iPad|iPhone|iPod/.test(userAgent)) {
        window.location.replace(I18n.t('app.get.apple'));
    } else if (/android/i.test(userAgent)) {
        window.location.replace(I18n.t('app.get.google'));
    }
    return null;
}

// public, everything but a phone gets the stores to choose from
export default function InstallApp() {
    return (
        <GuestLayout>
            <div className="install-app">
                <h1>{I18n.t('app.get.title')}</h1>
                <p>{I18n.t('app.get.info')}</p>
                <div className="install-app__stores">
                    <QRCodeSVG
                        value={`${window.location.origin}/install-app`}
                        size={120}
                        title={I18n.t('app.get.qr')}
                    />
                    <StoreBadges />
                </div>
            </div>
        </GuestLayout>
    );
}
