import { useEffect, useRef } from 'react';

import { FriendlyCaptchaSDK } from '@friendlycaptcha/sdk';

import styles from './Captcha.module.scss';

export type CaptchaProps = {
    siteKey: string;
    language: string;
    /** Receives the solved response token, or null when it is reset or expired. */
    onChange: (response: string | null) => void;
};

export const Captcha = ({ siteKey, language, onChange }: CaptchaProps) => {
    const mountRef = useRef<HTMLDivElement>(null);
    // Keeps the widget from being recreated when the parent passes a new callback
    const onChangeRef = useRef(onChange);

    useEffect(() => {
        onChangeRef.current = onChange;
    });

    useEffect(() => {
        const element = mountRef.current;
        if (!element) {
            return;
        }

        const widget = new FriendlyCaptchaSDK().createWidget({
            element,
            sitekey: siteKey,
            language,
            startMode: 'auto',
        });

        const controller = new AbortController();
        const clear = () => onChangeRef.current(null);
        const options = { signal: controller.signal };
        widget.addEventListener(
            'frc:widget.complete',
            (e) => onChangeRef.current(e.detail.response),
            options,
        );
        widget.addEventListener('frc:widget.expire', clear, options);
        widget.addEventListener('frc:widget.reset', clear, options);
        widget.addEventListener('frc:widget.error', clear, options);

        return () => {
            controller.abort();
            widget.destroy();
            // destroy() emits no reset/expire event, so a stale token would otherwise stay in the parent
            clear();
        };
    }, [siteKey, language]);

    return <div ref={mountRef} className={`frc-captcha ${styles.captcha}`} />;
};
