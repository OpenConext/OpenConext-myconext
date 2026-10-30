import { Button } from '@surfnet/curve-react';

import type { TiqrStartResponse } from '../../api';
import I18n from '../../locale/I18n';
import { ManualCodeEntry } from './ManualCodeEntry';
import styles from './route.module.scss';

const isTouchDevice = () =>
    typeof document !== 'undefined' &&
    'ontouchstart' in document.documentElement;

type QrScanProps = {
    start: TiqrStartResponse;
};

export const QrScan = ({ start }: QrScanProps) => {
    const qrImage = (
        <img
            className={styles.qr}
            src={start.qr}
            alt={I18n.t('useApp.qrAlt')}
            width={230}
            height={231}
        />
    );

    return (
        <>
            {isTouchDevice() ? (
                <div className={styles.qrMobile}>
                    <a href={start.url}>{qrImage}</a>
                    <Button
                        type="button"
                        onClick={() => (window.location.href = start.url)}
                    >
                        {I18n.t('useApp.openApp')}
                    </Button>
                </div>
            ) : (
                <div className={styles.qrWrapper}>{qrImage}</div>
            )}

            <ManualCodeEntry sessionKey={start.sessionKey} />
        </>
    );
};
