import { QRCodeSVG } from 'qrcode.react';

export type InstallAppQrProps = {
    title: string;
    // defaults to the /install-app route of the current app
    value?: string;
    size?: number;
};

// each app serves its own /install-app route that sends the phone to its store
export const InstallAppQr = ({
    title,
    value = `${window.location.origin}/install-app`,
    size = 120,
}: InstallAppQrProps) => (
    <QRCodeSVG
        value={value}
        size={size}
        title={title}
    />
);
