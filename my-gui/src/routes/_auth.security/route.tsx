import { useState } from 'react';
import { useSearchParams } from 'react-router';

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
import { useQuery } from '@tanstack/react-query';

import { fetchConfig, fetchMe } from '../../api';
import { ActivateAppDialog } from '../../components/ActivateAppDialog';
import { AddPasskeyDialog } from '../../components/AddPasskeyDialog';
import { DeactivateAppDialog } from '../../components/DeactivateAppDialog';
import { EditPasskeyDialog } from '../../components/EditPasskeyDialog';
import { LoginMethodRow } from '../../components/LoginMethodRow';
import { PageHeader } from '../../components/PageHeader';
import { PasswordDialog } from '../../components/PasswordDialog';
import I18n from '../../locale/I18n';
import { formatDate, formatDateTime } from '../../utils/format';
import './route.scss';

export default function Security() {
    const [showPassword, setShowPassword] = useState(false);
    const [showAddPasskey, setShowAddPasskey] = useState(false);
    const [showActivateApp, setShowActivateApp] = useState(false);
    const [showDeactivateApp, setShowDeactivateApp] = useState(false);
    const [searchParams, setSearchParams] = useSearchParams();
    // set by the redirect after a passkey test, or by the change button
    const [editingId, setEditingId] = useState<string | null>(null);
    const credentialId = editingId ?? searchParams.get('credential');
    const { data: me } = useQuery({ queryKey: ['me'], queryFn: fetchMe });
    const { data: config } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });

    if (!me) {
        return null;
    }

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
                        icon={<EnvelopeSimpleIcon />}
                        title={I18n.t('security.email.title')}
                        description={I18n.t('security.email.description', {
                            email: me.email,
                        })}
                        changeTo="/identity/edit-email"
                    />
                    {hasPassword && (
                        <LoginMethodRow
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

            {showApp && !hasApp && (
                <section className="security__section">
                    <h2 className="security__heading">
                        {I18n.t('security.recommended')}
                    </h2>
                    <div className="security__actions">
                        <Button
                            variant="ghost"
                            size="lg"
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
                                onClick={() => setShowAddPasskey(true)}
                            >
                                <KeyIcon />
                                {I18n.t('security.passkey.add')}
                            </Button>
                        )}
                    </div>
                </section>
            )}
            {showPassword && (
                <PasswordDialog
                    user={me}
                    onClose={() => setShowPassword(false)}
                />
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
