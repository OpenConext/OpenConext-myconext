import { useState } from 'react';
import type { FormEvent } from 'react';
import { Navigate, useNavigate, useParams } from 'react-router';

import { toast } from 'sonner';

import {
    Alert,
    AlertDescription,
    Button,
    Checkbox,
    Field,
    FieldError,
    FieldLabel,
    Input,
    Spinner,
} from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import {
    allowedEmailDomains,
    createInstitutionEduId,
    fetchInstitutionInfo,
    institutionalEmailDomains,
} from '../../api';
import { GuestLayout } from '../../components/GuestLayout';
import I18n from '../../locale/I18n';
import { useConfigStore } from '../../store/config';
import './route.scss';

const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// a domain matches itself and all its subdomains
const matches = (domains: string[] | undefined, domain: string) =>
    (domains ?? []).some(
        (name) => domain === name || domain.endsWith(`.${name}`),
    );

// public, the institution sent the user back with the hash of the flow
export default function LinkFromInstitution() {
    const { hash = '' } = useParams();
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [touched, setTouched] = useState(false);
    const [agreed, setAgreed] = useState(false);
    const [inUse, setInUse] = useState(false);
    const [forbidden, setForbidden] = useState(false);

    const config = useConfigStore((state) => state.config);
    const { data: info, isError } = useQuery({
        queryKey: ['institutionInfo', hash],
        queryFn: () => fetchInstitutionInfo(hash),
        retry: false,
    });
    const { data: institutional } = useQuery({
        queryKey: ['institutionalEmailDomains'],
        queryFn: institutionalEmailDomains,
        enabled: Boolean(config?.featureWarningEducationalEmailDomain),
    });
    const { data: allowed } = useQuery({
        queryKey: ['allowedEmailDomains'],
        queryFn: allowedEmailDomains,
        enabled: Boolean(config?.featureAllowList),
    });

    const trimmed = email.trim();
    const domain = trimmed.includes('@')
        ? trimmed.substring(trimmed.lastIndexOf('@') + 1).toLowerCase()
        : '';
    const notAllowed =
        Boolean(config?.featureAllowList) &&
        domain !== '' &&
        allowed !== undefined &&
        !matches(allowed, domain);
    const institutionalWarning =
        Boolean(config?.featureWarningEducationalEmailDomain) &&
        domain !== '' &&
        matches(institutional, domain);
    const valid = EMAIL.test(trimmed) && !notAllowed;

    const create = useMutation({
        mutationFn: (newUser: boolean) =>
            createInstitutionEduId(trimmed, hash, newUser),
        onSuccess: (_, newUser) => {
            toast.info(I18n.t('toast.emailCodeSent', { email: trimmed }));
            void navigate(`/create-from-institution/verify/${hash}`, {
                replace: true,
                state: { email: trimmed, newUser },
            });
        },
        onError: (error) => {
            if (error instanceof Response && error.status === 409) {
                setInUse(true);
            } else if (error instanceof Response && error.status === 412) {
                setForbidden(true);
            } else {
                void navigate('/create-from-institution/expired', {
                    replace: true,
                });
            }
        },
    });

    const submit = (event: FormEvent) => {
        event.preventDefault();
        setTouched(true);
        setInUse(false);
        setForbidden(false);
        if (valid && agreed && !create.isPending) {
            create.mutate(true);
        }
    };

    if (isError) {
        return <Navigate to="/create-from-institution/expired" replace />;
    }

    return (
        <GuestLayout>
            {!info ? (
                <Spinner />
            ) : (
                <form className="link-from-institution" onSubmit={submit}>
                    <h1>
                        {I18n.t('createFromInstitution.linkTitle', {
                            name: info.given_name ?? '',
                        })}
                    </h1>
                    <p>{I18n.t('createFromInstitution.linkInfo')}</p>
                    <Field>
                        <FieldLabel htmlFor="institutionEmail">
                            {I18n.t('createFromInstitution.email')}
                        </FieldLabel>
                        <Input
                            id="institutionEmail"
                            data-testid="link-from-institution-email"
                            type="email"
                            autoFocus
                            autoComplete="email"
                            placeholder={I18n.t(
                                'createFromInstitution.emailPlaceholder',
                            )}
                            value={email}
                            onChange={(e) => {
                                setEmail(e.target.value);
                                setInUse(false);
                                setForbidden(false);
                            }}
                        />
                        {touched && !EMAIL.test(trimmed) && (
                            <FieldError>
                                {I18n.t('createFromInstitution.invalidEmail')}
                            </FieldError>
                        )}
                        {notAllowed && (
                            <FieldError>
                                {I18n.t(
                                    'createFromInstitution.domainNotAllowed',
                                    {
                                        domain,
                                    },
                                )}
                            </FieldError>
                        )}
                        {inUse && (
                            <FieldError>
                                {I18n.t('createFromInstitution.emailInUse')}{' '}
                                <button
                                    type="button"
                                    className="link-from-institution__link"
                                    onClick={() => create.mutate(false)}
                                >
                                    {I18n.t(
                                        'createFromInstitution.emailInUseLink',
                                    )}
                                </button>
                            </FieldError>
                        )}
                        {forbidden && (
                            <FieldError>
                                {I18n.t('createFromInstitution.emailForbidden')}
                            </FieldError>
                        )}
                    </Field>
                    {institutionalWarning && !notAllowed && (
                        <Alert variant="warning">
                            <AlertDescription>
                                {I18n.t(
                                    'createFromInstitution.institutionDomainWarning',
                                )}
                            </AlertDescription>
                        </Alert>
                    )}
                    <label className="link-from-institution__terms">
                        <Checkbox
                            data-testid="link-from-institution-agree"
                            checked={agreed}
                            onCheckedChange={(checked) =>
                                setAgreed(checked === true)
                            }
                        />
                        <span>
                            {I18n.t('createFromInstitution.agreeStart')}
                            <a
                                href={I18n.t('createFromInstitution.termsUrl')}
                                target="_blank"
                                rel="noreferrer"
                            >
                                {I18n.t('createFromInstitution.termsLink')}
                            </a>
                            {I18n.t('createFromInstitution.agreeMiddle')}
                            <a
                                href={I18n.t(
                                    'createFromInstitution.privacyUrl',
                                )}
                                target="_blank"
                                rel="noreferrer"
                            >
                                {I18n.t('createFromInstitution.privacyLink')}
                            </a>
                            {I18n.t('createFromInstitution.agreeEnd')}
                        </span>
                    </label>
                    <Button
                        type="submit"
                        data-testid="link-from-institution-submit"
                        disabled={!valid || !agreed || create.isPending}
                    >
                        {create.isPending && <Spinner />}
                        {I18n.t('createFromInstitution.request')}
                    </Button>
                </form>
            )}
        </GuestLayout>
    );
}
