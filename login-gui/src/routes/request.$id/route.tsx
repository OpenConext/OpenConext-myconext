import { type FC, type ReactNode, useState } from 'react';
import { Link, useParams } from 'react-router';

import { Captcha, sanitizeHtml } from '@myconext/shared-gui';
import {
    Button,
    Checkbox,
    Field,
    FieldLabel,
    Input,
} from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { fetchConfig } from '../../api';
import { LoginContainer } from '../../components';
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
    const { id } = useParams();

    const [email, setEmail] = useState('');
    const [givenName, setGivenName] = useState('');
    const [familyName, setFamilyName] = useState('');
    const [agreed, setAgreed] = useState(false);
    const [captchaResponse, setCaptchaResponse] = useState<string | null>(null);

    const { data: config, isPending } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });
    const captchaSiteKey = config?.captchaEnabled
        ? config.captchaSiteKey
        : undefined;
    const captchaPending = isPending || (!!captchaSiteKey && !captchaResponse);

    return (
        <Container id={id}>
            <form
                className={styles.form}
                onSubmit={(e) => {
                    e.preventDefault();
                    // TODO: submit the registration request, including captchaResponse
                }}
            >
                <Field>
                    <FieldLabel htmlFor="email">
                        {I18n.t('request.email')}
                    </FieldLabel>
                    <Input
                        id="email"
                        type="email"
                        autoComplete="email"
                        required
                        value={email}
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
                        onChange={(e) => setFamilyName(e.target.value)}
                    />
                </Field>
                <label className={styles.terms}>
                    <Checkbox
                        checked={agreed}
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
                {captchaSiteKey && (
                    <Captcha
                        siteKey={captchaSiteKey}
                        language={I18n.locale}
                        onChange={setCaptchaResponse}
                    />
                )}
                <Button type="submit" disabled={!agreed || captchaPending}>
                    {I18n.t('request.submit')}
                </Button>
            </form>
        </Container>
    );
}
