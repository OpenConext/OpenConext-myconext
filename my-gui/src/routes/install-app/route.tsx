import I18n from '../../locale/I18n';

// opened from the QR code in the activate app dialog, sends the phone to its app store
// eslint-disable-next-line react-refresh/only-export-components
export function clientLoader() {
    const userAgent = navigator.userAgent;
    if (/iPad|iPhone|iPod/.test(userAgent)) {
        window.location.replace(I18n.t('app.get.apple'));
    } else if (/android/i.test(userAgent)) {
        window.location.replace(I18n.t('app.get.google'));
    } else {
        window.location.replace('/security');
    }
    return null;
}

export default function InstallApp() {
    return null;
}
