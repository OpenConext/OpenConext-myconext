import {
    type FC,
    type ReactNode,
    type SubmitEvent,
    useEffect,
    useRef,
    useState,
} from 'react';
import { Link, useNavigate, useParams } from 'react-router';

import { sanitizeHtml } from '@myconext/shared-gui';
import { Checkbox, Field, FieldLabel, Input } from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import {
    configQueryOptions,
    fetchServiceName,
    generateCodeNewUser,
} from '../../api';
import {
    Captcha,
    ErrorAlert,
    LoadingButton,
    LoginContainer,
} from '../../components';
import I18n from '../../locale/I18n.ts';
import styles from './route.module.scss';

const Container: FC<{ children: ReactNode; id?: string }> = ({
    children,
    id,
}) => (
    <LoginContainer
        title={I18n.t('request.title')}
        footer={
            <>
                <p>{I18n.t('request.hasAccount')}</p>
                <Link to={`/login/${id}`}>{I18n.t('request.login')}</Link>
            </>
        }
    >
        {children}
    </LoginContainer>
);

export default function Request() {
    const { id = '' } = useParams();
    const navigate = useNavigate();

    const [email, setEmail] = useState('');
    const [givenName, setGivenName] = useState('');
    const [familyName, setFamilyName] = useState('');
    const [agreed, setAgreed] = useState(false);
    const emailInputRef = useRef<HTMLInputElement>(null);

    // Captcha tokens are single-use; remounting the widget yields a fresh one after a rejected attempt.
    const [captchaKey, setCaptchaKey] = useState(0);
    const [captchaResponse, setCaptchaResponse] = useState<string | null>(null);

    const { data: config, isPending } = useQuery(configQueryOptions);
    const captchaSiteKey = config?.captchaEnabled
        ? config.captchaSiteKey
        : undefined;
    const { data: service } = useQuery({
        queryKey: ['serviceName', id],
        queryFn: () => fetchServiceName(id),
        enabled: !!id,
    });

    const {
        mutate: submit,
        isPending: isMutating,
        isSuccess,
        error,
    } = useMutation({
        mutationFn: () =>
            generateCodeNewUser(
                { email, givenName, familyName },
                id,
                captchaResponse,
            ),
        onSuccess: (res) => {
            if (res?.stepup) {
                const params = new URLSearchParams({
                    name: service?.name ?? '',
                    explanation: res.explanation ?? '',
                });
                navigate(`/stepup/${id}?${params}`, { replace: true });
                return;
            }
            // The server already sent the code, so /code must not request another one.
            navigate(`/code/${id}`, {
                replace: true,
                state: { email, codeSent: true },
            });
        },
        onError: (e) => {
            setCaptchaKey((k) => k + 1);
            if (
                e instanceof Response &&
                (e.status === 409 || e.status === 412)
            ) {
                return;
            }
            navigate('/expired', { replace: true });
        },
    });
    const isSubmitting = isMutating || isSuccess;

    useEffect(() => {
        if (!isSubmitting) {
            emailInputRef.current?.focus();
        }
    }, [isSubmitting]);

    const errorStatus = error instanceof Response ? error.status : undefined;
    const captchaPending = isPending || (!!captchaSiteKey && !captchaResponse);

    const isValidForm =
        email.trim() !== '' &&
        givenName.trim() !== '' &&
        familyName.trim() !== '' &&
        agreed &&
        !captchaPending;

    const handleSubmit = (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (isSubmitting || !isValidForm) {
            return;
        }
        submit();
    };

    return (
        <Container id={id}>
            <form className={styles.form} onSubmit={handleSubmit}>
                <Field>
                    <FieldLabel htmlFor="email">
                        {I18n.t('request.email')}
                    </FieldLabel>
                    <Input
                        id="email"
                        ref={emailInputRef}
                        type="email"
                        autoComplete="email"
                        autoFocus
                        required
                        value={email}
                        disabled={isSubmitting}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                </Field>
                <Field>
                    <FieldLabel htmlFor="givenName">
                        {I18n.t('request.givenName')}
                    </FieldLabel>
                    <Input
                        id="givenName"
                        type="text"
                        autoComplete="given-name"
                        required
                        value={givenName}
                        disabled={isSubmitting}
                        onChange={(e) => setGivenName(e.target.value)}
                    />
                </Field>
                <Field>
                    <FieldLabel htmlFor="familyName">
                        {I18n.t('request.familyName')}
                    </FieldLabel>
                    <Input
                        id="familyName"
                        type="text"
                        autoComplete="family-name"
                        required
                        value={familyName}
                        disabled={isSubmitting}
                        onChange={(e) => setFamilyName(e.target.value)}
                    />
                </Field>
                <label className={styles.terms}>
                    <Checkbox
                        checked={agreed}
                        disabled={isSubmitting}
                        onCheckedChange={(checked) =>
                            setAgreed(checked === true)
                        }
                    />
                    <span
                        dangerouslySetInnerHTML={{
                            __html: sanitizeHtml(I18n.t('request.agree')),
                        }}
                    />
                </label>
                {errorStatus === 409 && (
                    <ErrorAlert>
                        {I18n.t('request.emailInUse')}{' '}
                        <Link to={`/login/${id}`}>
                            {I18n.t('request.login')}
                        </Link>
                    </ErrorAlert>
                )}
                {errorStatus === 412 && (
                    <ErrorAlert>{I18n.t('request.emailNotAllowed')}</ErrorAlert>
                )}
                {captchaSiteKey && (
                    <Captcha
                        key={captchaKey}
                        siteKey={captchaSiteKey}
                        language={I18n.locale}
                        onChange={setCaptchaResponse}
                    />
                )}
                <LoadingButton
                    type="submit"
                    loading={isSubmitting}
                    disabled={!isValidForm}
                >
                    {I18n.t('request.submit')}
                </LoadingButton>
            </form>
        </Container>
    );
}
