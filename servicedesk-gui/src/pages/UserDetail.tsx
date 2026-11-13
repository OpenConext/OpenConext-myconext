import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';

import { toast } from 'sonner';

import {
    ArrowLeftIcon,
    BuildingOfficeIcon,
    CaretDownIcon,
    CheckCircleIcon,
    DeviceMobileSpeakerIcon,
    KeyIcon,
    LockKeyOpenIcon,
    PasswordIcon,
    PlusCircleIcon,
    ProhibitIcon,
    SealCheckIcon,
    ShieldCheckIcon,
    UserCircleGearIcon,
} from '@phosphor-icons/react';
import {
    Badge,
    Button,
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuRadioGroup,
    DropdownMenuRadioItem,
    DropdownMenuTrigger,
    Spinner,
} from '@surfnet/curve-react';

import {
    type ServiceDeskUserDetail,
    blockUser,
    getUserDetail,
    resetMfa,
    unblockUser,
} from '../api';
import { BlockUserDialog } from '../components/BlockUserDialog';
import { PageHeader } from '../components/PageHeader';
import { ResetMfaDialog } from '../components/ResetMfaDialog';
import { UnblockUserDialog } from '../components/UnblockUserDialog';
import I18n from '../locale/I18n';
import { useAppStore } from '../stores/AppStore';
import {
    ACTIVITY_TYPES,
    type ActivityType,
    activityText,
    activityType,
} from '../utils/activity';
import './UserDetail.scss';

const ACTIVITY_PAGE_SIZE = 5;

const ACTIVITY_ICONS = {
    account: UserCircleGearIcon,
    security: LockKeyOpenIcon,
    institution: BuildingOfficeIcon,
    service: LockKeyOpenIcon,
};

// events with an icon of their own
const EVENT_ICONS: Record<string, typeof KeyIcon> = {
    BLOCKED_ACCOUNT: ProhibitIcon,
    DEBLOCKED_ACCOUNT: SealCheckIcon,
};

const MFA_ICONS: Record<string, typeof KeyIcon> = {
    app: DeviceMobileSpeakerIcon,
    passkey: KeyIcon,
    password: PasswordIcon,
};

const affiliationLabel = (affiliation: string) =>
    I18n.t(`users.affiliation.${affiliation}`, { defaultValue: affiliation });

const formatDate = (value?: number) =>
    value
        ? new Intl.DateTimeFormat('nl-NL', {
              day: '2-digit',
              month: '2-digit',
              year: 'numeric',
              timeZone: 'UTC',
          }).format(new Date(value))
        : '';

const formatDateTime = (value: number) =>
    new Intl.DateTimeFormat(I18n.locale === 'nl' ? 'nl-NL' : 'en-GB', {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    }).format(new Date(value));

type FieldProps = {
    label: string;
    value?: string;
    verified?: boolean;
};

const Field = ({ label, value, verified }: FieldProps) => (
    <div className="user-detail__field">
        <span className="user-detail__label">{label}</span>
        <span className="user-detail__value">
            {value || '–'}
            {verified && value && (
                <ShieldCheckIcon
                    className="user-detail__verified"
                    aria-label={I18n.t('userDetail.verified')}
                />
            )}
        </span>
    </div>
);

const userName = (user: ServiceDeskUserDetail | null) =>
    [user?.chosenName || user?.givenName, user?.familyName]
        .filter(Boolean)
        .join(' ');

export const UserDetail = () => {
    const { id } = useParams();
    const [user, setUser] = useState<ServiceDeskUserDetail | null>(null);
    const [failed, setFailed] = useState(false);
    const [type, setType] = useState<ActivityType | 'all'>('all');
    const [shown, setShown] = useState(ACTIVITY_PAGE_SIZE);

    const [blocking, setBlocking] = useState(false);
    const [blockPending, setBlockPending] = useState(false);
    const [blockFailed, setBlockFailed] = useState(false);
    const institution = useAppStore(
        (state) => state.user?.schacHomeOrganization,
    );

    const [resettingMfa, setResettingMfa] = useState(false);
    const [resetPending, setResetPending] = useState(false);
    const [resetFailed, setResetFailed] = useState(false);

    // incremented to reload the details of the user
    const [reloads, setReloads] = useState(0);

    useEffect(() => {
        const load = async () => {
            try {
                setUser(await getUserDetail(id ?? ''));
                setFailed(false);
            } catch {
                setFailed(true);
            }
        };
        void load();
    }, [id, reloads]);

    const block = async () => {
        setBlockPending(true);
        setBlockFailed(false);
        try {
            const wasBlocked = user?.blocked;
            await (wasBlocked ? unblockUser : blockUser)(id ?? '');
            toast.success(
                I18n.t(
                    wasBlocked
                        ? 'userDetail.unblockedToast'
                        : 'userDetail.blockedToast',
                    { name: userName(user) },
                ),
            );
            // reload the details of the user after the user is (un)blocked
            setReloads((count) => count + 1);
            setBlocking(false);
        } catch {
            setBlockFailed(true);
        } finally {
            setBlockPending(false);
        }
    };

    const reset = async () => {
        setResetPending(true);
        setResetFailed(false);
        try {
            await resetMfa(id ?? '');
            toast.success(
                I18n.t('userDetail.mfaResetToast', { name: userName(user) }),
            );
            // reload the details of the user after the MFA methods are reset
            setReloads((count) => count + 1);
            setResettingMfa(false);
        } catch {
            setResetFailed(true);
        } finally {
            setResetPending(false);
        }
    };

    if (failed) {
        return (
            <div className="user-detail">
                <PageHeader>{I18n.t('userDetail.notFound')}</PageHeader>
                <div className="user-detail__content">
                    <Link className="user-detail__back" to="/users">
                        <ArrowLeftIcon />
                        {I18n.t('userDetail.back')}
                    </Link>
                </div>
            </div>
        );
    }
    if (!user) {
        return <Spinner className="loading" />;
    }

    const name = userName(user);
    const activities = user.activities.filter(
        (activity) => type === 'all' || activityType(activity.event) === type,
    );

    return (
        <div className="user-detail">
            <PageHeader
                subtitle={
                    <span className="user-detail__badges">
                        {user.blocked && (
                            <Badge variant="danger">
                                <ProhibitIcon />
                                {I18n.t('userDetail.blocked')}
                            </Badge>
                        )}
                        {user.affiliations.map((affiliation) => (
                            <Badge key={affiliation} variant="success">
                                <CheckCircleIcon />
                                {affiliationLabel(affiliation)}
                            </Badge>
                        ))}
                        {user.verifiedBy.map((issuer) => (
                            <Badge key={issuer} variant="success">
                                <ShieldCheckIcon />
                                {I18n.t('userDetail.verifiedWith', {
                                    issuer: I18n.t(`issuers.${issuer}`, {
                                        defaultValue: issuer,
                                    }),
                                })}
                            </Badge>
                        ))}
                    </span>
                }
                action={
                    <Button
                        variant="outline"
                        onClick={() => {
                            setBlockFailed(false);
                            setBlocking(true);
                        }}
                    >
                        {user.blocked ? <SealCheckIcon /> : <ProhibitIcon />}
                        {I18n.t(
                            user.blocked
                                ? 'userDetail.unblock'
                                : 'userDetail.block',
                        )}
                    </Button>
                }
            >
                {name}
            </PageHeader>
            {blocking && user.blocked && (
                <UnblockUserDialog
                    name={name}
                    institution={institution}
                    isPending={blockPending}
                    isError={blockFailed}
                    onConfirm={() => void block()}
                    onClose={() => setBlocking(false)}
                />
            )}
            {blocking && !user.blocked && (
                <BlockUserDialog
                    name={name}
                    institution={institution}
                    isPending={blockPending}
                    isError={blockFailed}
                    onConfirm={() => void block()}
                    onClose={() => setBlocking(false)}
                />
            )}
            {resettingMfa && (
                <ResetMfaDialog
                    name={name}
                    givenName={user.institutionGivenName}
                    familyName={user.institutionFamilyName}
                    isPending={resetPending}
                    isError={resetFailed}
                    onConfirm={() => void reset()}
                    onClose={() => setResettingMfa(false)}
                />
            )}
            <div className="user-detail__content">
                <div className="user-detail__card">
                    <div className="user-detail__row">
                        <Field
                            label={I18n.t('userDetail.givenName')}
                            value={user.givenName}
                            verified={user.namesVerified}
                        />
                        <Field
                            label={I18n.t('userDetail.familyName')}
                            value={user.familyName}
                            verified={user.namesVerified}
                        />
                    </div>
                    <div className="user-detail__row">
                        <Field
                            label={I18n.t('userDetail.chosenName')}
                            value={user.chosenName}
                        />
                        <Field
                            label={I18n.t('userDetail.dateOfBirth')}
                            value={formatDate(user.dateOfBirth)}
                            verified={!!user.dateOfBirth}
                        />
                    </div>
                    <div className="user-detail__row">
                        <Field
                            label={I18n.t('userDetail.id')}
                            value={user.uid}
                        />
                        <Field
                            label={I18n.t('userDetail.eppn')}
                            value={user.eduPersonPrincipalName}
                        />
                    </div>
                    <div className="user-detail__row user-detail__row--action">
                        <div className="user-detail__field">
                            <span className="user-detail__label">
                                {I18n.t('userDetail.mfa')}
                            </span>
                            <span className="user-detail__badges">
                                {user.mfaMethods.length === 0
                                    ? '–'
                                    : user.mfaMethods.map((method) => {
                                          const Icon =
                                              MFA_ICONS[method] ?? KeyIcon;
                                          return (
                                              <Badge
                                                  key={method}
                                                  variant="info"
                                              >
                                                  <Icon />
                                                  {I18n.t(
                                                      `users.mfa.${method}`,
                                                  )}
                                              </Badge>
                                          );
                                      })}
                            </span>
                        </div>
                        {/* only the mobile app can be reset, it is gone after a reset */}
                        {user.mfaMethods.includes('app') && (
                            <Button
                                variant="link"
                                onClick={() => {
                                    setResetFailed(false);
                                    setResettingMfa(true);
                                }}
                            >
                                {I18n.t('userDetail.reset')}
                            </Button>
                        )}
                    </div>
                    <div className="user-detail__row user-detail__row--action">
                        <Field
                            label={I18n.t('userDetail.loginCode')}
                            value={user.email}
                        />
                        {/* TODO: changing the email address is implemented later */}
                        <Button variant="link">
                            {I18n.t('userDetail.change')}
                        </Button>
                    </div>
                </div>

                <div className="user-detail__activity-header">
                    <h2>{I18n.t('activity.title')}</h2>
                    <DropdownMenu>
                        <DropdownMenuTrigger
                            render={
                                <Button
                                    variant="outline"
                                    className="user-detail__filter"
                                />
                            }
                        >
                            {I18n.t(
                                type === 'all'
                                    ? 'activity.filter'
                                    : `activity.types.${type}`,
                            )}
                            <CaretDownIcon />
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                            <DropdownMenuRadioGroup
                                value={type}
                                onValueChange={(value) => {
                                    setType(value as ActivityType | 'all');
                                    setShown(ACTIVITY_PAGE_SIZE);
                                }}
                            >
                                <DropdownMenuRadioItem value="all">
                                    {I18n.t('activity.allTypes')}
                                </DropdownMenuRadioItem>
                                {ACTIVITY_TYPES.map((activityKind) => (
                                    <DropdownMenuRadioItem
                                        key={activityKind}
                                        value={activityKind}
                                    >
                                        {I18n.t(
                                            `activity.types.${activityKind}`,
                                        )}
                                    </DropdownMenuRadioItem>
                                ))}
                            </DropdownMenuRadioGroup>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
                <div className="user-detail__card user-detail__activities">
                    {activities.length === 0 && (
                        <p className="user-detail__empty">
                            {I18n.t('activity.empty')}
                        </p>
                    )}
                    {activities.slice(0, shown).map((activity, index) => {
                        const Icon =
                            EVENT_ICONS[activity.event] ??
                            ACTIVITY_ICONS[activityType(activity.event)];
                        return (
                            <div
                                key={`${activity.event}-${activity.dateTime}-${index}`}
                                className="user-detail__activity"
                            >
                                <span className="user-detail__activity-text">
                                    <Icon />
                                    {activityText(activity)}
                                </span>
                                <span className="user-detail__activity-time">
                                    {formatDateTime(activity.dateTime)}
                                </span>
                            </div>
                        );
                    })}
                    {activities.length > shown && (
                        <div className="user-detail__more">
                            <Button
                                variant="link"
                                onClick={() =>
                                    setShown(shown + ACTIVITY_PAGE_SIZE)
                                }
                            >
                                <PlusCircleIcon />
                                {I18n.t('activity.showMore')}
                            </Button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};
