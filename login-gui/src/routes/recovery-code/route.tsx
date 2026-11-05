import { useEffect, useRef, useState } from 'react';
import { Navigate, useSearchParams } from 'react-router';

import { formatRecoveryCode } from '@myconext/shared-gui';
import { Button } from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import {
    type BackupCodeResponse,
    configQueryOptions,
    generateBackupCode,
} from '../../api';
import { Loading, LoginContainer, RecoveryCodeView } from '../../components';
import I18n from '../../locale/I18n';
import { rememberLoginMethod } from '../../utils/loginMethods';
import { getProceedUrl } from '../../utils/sso';

const storageKey = (hash: string) => `recovery-code:${hash}`;

const isBackupCode = (value: unknown): value is BackupCodeResponse =>
    typeof value === 'object' &&
    value !== null &&
    'recoveryCode' in value &&
    typeof value.recoveryCode === 'string' &&
    'redirect' in value &&
    typeof value.redirect === 'string';

// The server only hands out the code once, so a reload falls back on this copy
const readStoredCode = (hash: string): BackupCodeResponse | undefined => {
    try {
        const stored: unknown = JSON.parse(
            sessionStorage.getItem(storageKey(hash)) ?? 'null',
        );

        return isBackupCode(stored) ? stored : undefined;
    } catch {
        return undefined;
    }
};

export default function RecoveryCode() {
    const [searchParams] = useSearchParams();
    const hash = searchParams.get('h') ?? '';

    // generate-backup-code marks the login as an app flow, so it must fire once
    const hasFired = useRef(false);
    const [storedCode] = useState(() => readStoredCode(hash));

    const { data: config } = useQuery(configQueryOptions);
    const [proceedFailed, setProceedFailed] = useState(false);

    const {
        mutate: generate,
        data: generated,
        isError,
    } = useMutation({
        mutationFn: () => generateBackupCode(hash),
        onSuccess: (code) =>
            sessionStorage.setItem(storageKey(hash), JSON.stringify(code)),
    });

    useEffect(() => {
        if (!hash || hasFired.current) {
            return;
        }

        hasFired.current = true;
        generate();
    }, [hash, generate]);

    if (!hash) {
        return <Navigate to="/expired" replace />;
    }

    const code = generated ?? (isError ? storedCode : undefined);

    if (isError && !code) {
        return (
            <LoginContainer
                title={I18n.t('recovery.backupTitle')}
                description={I18n.t('recovery.error')}
                headerAlign="start"
            >
                <Button type="button" onClick={() => window.location.reload()}>
                    {I18n.t('recovery.retry')}
                </Button>
            </LoginContainer>
        );
    }

    if (!code) {
        return <Loading title={I18n.t('recovery.backupTitle')} />;
    }

    // Validated on click: the one-time code must be shown even if this fails
    const proceed = (): boolean => {
        const proceedUrl = getProceedUrl(
            new URLSearchParams({ h: hash, redirect: code.redirect }),
            config?.magicLinkUrl ?? '',
        );
        if (!proceedUrl) {
            setProceedFailed(true);
            return false;
        }

        rememberLoginMethod('useApp');
        window.location.href = proceedUrl;
        return true;
    };

    return (
        <RecoveryCodeView
            recoveryCode={formatRecoveryCode(code.recoveryCode)}
            proceedFailed={proceedFailed}
            onNext={proceed}
        />
    );
}
