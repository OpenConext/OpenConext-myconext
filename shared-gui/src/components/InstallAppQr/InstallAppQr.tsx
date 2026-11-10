import { QRCodeSVG } from 'qrcode.react';

export type InstallAppQrProps = {
    title: string;
};

// each app serves its own /install-app route that sends the phone to its store
export const InstallAppQr = ({ title }: InstallAppQrProps) => (
    <QRCodeSVG
        value={`${window.location.origin}/install-app`}
        size={120}
        title={title}
    />
);
