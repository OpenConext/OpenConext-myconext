import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router';

import { toast } from 'sonner';

import {
    DeviceMobileSpeakerIcon,
    EnvelopeSimpleIcon,
    KeyIcon,
    PasswordIcon,
    ShieldCheckIcon,
} from '@phosphor-icons/react';
import {
    Alert,
    AlertDescription,
    AlertTitle,
    Button,
} from '@surfnet/curve-react';

import { ActivateAppDialog } from '../../components/ActivateAppDialog';
import { AddPasskeyDialog } from '../../components/AddPasskeyDialog';
import { ChangeRecoveryDialog } from '../../components/ChangeRecoveryDialog';
import { DeactivateAppDialog } from '../../components/DeactivateAppDialog';
import { EditEmailDialog } from '../../components/EditEmailDialog';
import { EditPasskeyDialog } from '../../components/EditPasskeyDialog';
import { LoginMethodRow } from '../../components/LoginMethodRow';
import { PageHeader } from '../../components/PageHeader';
import { PasswordDialog } from '../../components/PasswordDialog';
import I18n from '../../locale/I18n';
import { useConfigStore } from '../../store/config';
import { useUserStore } from '../../store/me';
import { formatDate, formatDateTime } from '../../utils/format';
import './route.scss';

export default function Security() {
    const [showPassword, setShowPassword] = useState(false);
    const [showAddPasskey, setShowAddPasskey] = useState(false);
    const [showActivateApp, setShowActivateApp] = useState(false);
    const [showEditEmail, setShowEditEmail] = useState(false);
    const [showChangeRecovery, setShowChangeRecovery] = useState(false);
    const [showDeactivateApp, setShowDeactivateApp] = useState(false);
    const [searchParams, setSearchParams] = useSearchParams();
    // set by the redirect after a passkey test, or by the change button
    const [editingId, setEditingId] = useState<string | null>(null);
    const credentialId = editingId ?? searchParams.get('credential');
    const me = useUserStore((state) => state.user);
    const config = useConfigStore((state) => state.config);

    // after creating an eduID from an institution the server sends the user here
    const welcome = searchParams.get('new');
    useEffect(() => {
        if (welcome !== null) {
            toast.success(
                I18n.t(
                    welcome === 'false'
                        ? 'toast.welcomeExisting'
                        : 'toast.welcome',
                ),
            );
            setSearchParams({}, { replace: true });
        }
    }, [welcome, setSearchParams]);

    if (!me) {
        return null;
    }

    // mailed links and the old paths open the dialogs through the query string
    const passwordHash = searchParams.get('passwordHash') ?? undefined;
    const passwordOpen =
        showPassword || Boolean(passwordHash) || searchParams.has('password');
    const editEmailOpen = showEditEmail || searchParams.has('editEmail');
    const closeDialog = () => {
        setShowPassword(false);
        setShowEditEmail(false);
        if (searchParams.size > 0) {
            setSearchParams({}, { replace: true });
        }
    };

    const showApp = Boolean(config?.useApp);
    const hasApp =
        showApp &&
        (me.loginOptions ?? []).includes('useApp') &&
        Boolean(me.registration?.notificationType);
    const passkeys = config?.featureWebAuthn
        ? (me.publicKeyCredentials ?? [])
        : [];
    const hasPassword = Boolean(me.usePassword);
    const editedPasskey = passkeys.find(
        (passkey) => passkey.identifier === credentialId,
    );
    const testSuccess = searchParams.get('success');
    const testResult =
        editingId || !testSuccess
            ? undefined
            : testSuccess === 'true'
              ? 'success'
              : 'failed';
    // the login code by email is always available
    const activeCount =
        1 + (hasApp ? 1 : 0) + (hasPassword ? 1 : 0) + passkeys.length;
    const showOther = !hasPassword || Boolean(config?.featureWebAuthn);

    return (
        <div className="security">
            <PageHeader
                color="blue"
                icon={<ShieldCheckIcon />}
                title={I18n.t('security.title')}
                lead={I18n.t('security.lead')}
            />

            {activeCount === 1 && (
                <Alert variant="info" className="security__alert">
                    <KeyIcon />
                    <AlertTitle>{I18n.t('security.single.title')}</AlertTitle>
                    <AlertDescription>
                        {I18n.t('security.single.description')}
                    </AlertDescription>
                </Alert>
            )}

            <section className="security__section">
                <h2 className="security__heading">
                    {I18n.t('security.active')}
                </h2>
                <div className="security__card">
                    {hasApp && (
                        <LoginMethodRow
                            testId="security-app"
                            icon={<DeviceMobileSpeakerIcon />}
                            title={I18n.t('security.app.title')}
                            description={I18n.t('security.app.description')}
                            details={
                                <>
                                    <dl className="login-method__details">
                                        <dt>
                                            {I18n.t('security.app.lastLogin')}
                                        </dt>
                                        <dd>
                                            {me.registration?.updated
                                                ? formatDateTime(
                                                      me.registration.updated,
                                                  )
                                                : '-'}
                                        </dd>
                                        <dt>{I18n.t('security.app.device')}</dt>
                                        <dd>
                                            {I18n.t(
                                                `security.app.devices.${me.registration?.notificationType?.split('_')[0]}`,
                                                { defaultValue: '-' },
                                            )}
                                        </dd>
                                    </dl>
                                    <Button
                                        variant="ghost"
                                        data-testid="security-app-deactivate"
                                        className="login-method__action"
                                        onClick={() =>
                                            setShowDeactivateApp(true)
                                        }
                                    >
                                        {I18n.t('security.app.deactivate')}
                                    </Button>
                                </>
                            }
                        />
                    )}
                    <LoginMethodRow
                        testId="security-email"
                        icon={<EnvelopeSimpleIcon />}
                        title={I18n.t('security.email.title')}
                        description={I18n.t('security.email.description', {
                            email: me.email,
                        })}
                        onChange={() => setShowEditEmail(true)}
                    />
                    {hasPassword && (
                        <LoginMethodRow
                            testId="security-password"
                            icon={<PasswordIcon />}
                            title={I18n.t('security.password.title')}
                            description={
                                me.passwordUpdatedAt
                                    ? I18n.t('security.password.description', {
                                          date: formatDate(
                                              me.passwordUpdatedAt,
                                          ),
                                      })
                                    : ''
                            }
                            onChange={() => setShowPassword(true)}
                        />
                    )}
                    {passkeys.map((passkey) => (
                        <LoginMethodRow
                            key={passkey.identifier}
                            testId="security-passkey"
                            icon={<KeyIcon />}
                            title={I18n.t('security.passkey.title')}
                            description={I18n.t(
                                'security.passkey.description',
                                {
                                    name: passkey.name,
                                    date: formatDate(passkey.createdAt),
                                },
                            )}
                            onChange={() => setEditingId(passkey.identifier)}
                        />
                    ))}
                </div>
            </section>

            {hasApp && (
                <section className="security__section">
                    <h2 className="security__heading">
                        {I18n.t('security.recovery.title')}
                    </h2>
                    <div className="security__card">
                        <LoginMethodRow
                            icon={
                                me.registration?.recoveryCode ? (
                                    <KeyIcon />
                                ) : (
                                    <DeviceMobileSpeakerIcon />
                                )
                            }
                            title={I18n.t(
                                me.registration?.recoveryCode
                                    ? 'security.recovery.code'
                                    : 'security.recovery.phone',
                            )}
                            description={I18n.t(
                                me.registration?.recoveryCode
                                    ? 'security.recovery.codeInfo'
                                    : 'security.recovery.phoneInfo',
                                { phone: me.registration?.phoneNumber },
                            )}
                            onChange={() => setShowChangeRecovery(true)}
                        />
                    </div>
                </section>
            )}

            {showApp && !hasApp && (
                <section className="security__section">
                    <h2 className="security__heading">
                        {I18n.t('security.recommended')}
                    </h2>
                    <div className="security__actions">
                        <Button
                            variant="ghost"
                            size="lg"
                            data-testid="security-activate-app"
                            className="security__action"
                            onClick={() => setShowActivateApp(true)}
                        >
                            <DeviceMobileSpeakerIcon />
                            {I18n.t('security.app.download')}
                        </Button>
                    </div>
                </section>
            )}

            {showOther && (
                <section className="security__section">
                    <h2 className="security__heading">
                        {I18n.t('security.other')}
                    </h2>
                    <div className="security__actions">
                        {!hasPassword && (
                            <Button
                                variant="ghost"
                                className="security__action"
                                data-testid="security-add-password"
                                onClick={() => setShowPassword(true)}
                            >
                                <PasswordIcon />
                                {I18n.t('security.password.add')}
                            </Button>
                        )}
                        {config?.featureWebAuthn && (
                            <Button
                                variant="ghost"
                                className="security__action"
                                data-testid="security-add-passkey"
                                onClick={() => setShowAddPasskey(true)}
                            >
                                <KeyIcon />
                                {I18n.t('security.passkey.add')}
                            </Button>
                        )}
                    </div>
                </section>
            )}
            {passwordOpen && (
                <PasswordDialog
                    user={me}
                    initialHash={passwordHash}
                    onClose={closeDialog}
                />
            )}
            {editEmailOpen && (
                <EditEmailDialog user={me} onClose={closeDialog} />
            )}
            {showAddPasskey && (
                <AddPasskeyDialog onClose={() => setShowAddPasskey(false)} />
            )}
            {editedPasskey && (
                <EditPasskeyDialog
                    credential={editedPasskey}
                    testResult={testResult}
                    onClose={() => {
                        setEditingId(null);
                        setSearchParams({}, { replace: true });
                    }}
                />
            )}
            {showChangeRecovery && (
                <ChangeRecoveryDialog
                    onClose={() => setShowChangeRecovery(false)}
                />
            )}
            {showActivateApp && (
                <ActivateAppDialog onClose={() => setShowActivateApp(false)} />
            )}
            {showDeactivateApp && (
                <DeactivateAppDialog
                    user={me}
                    onClose={() => setShowDeactivateApp(false)}
                />
            )}
        </div>
    );
}
