import { Button } from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { configQueryOptions } from '../../api';
import {
    ErrorAlert,
    LoadingLoginContainer,
    LoginContainer,
} from '../../components';
import I18n from '../../locale/I18n.ts';

export default function Expired() {
    const { data: config, isPending } = useQuery(configQueryOptions);

    if (isPending) {
        return <LoadingLoginContainer title={I18n.t('expired.title')} />;
    }

    const homeUrl = config?.baseDomain ? `https://${config.baseDomain}` : null;

    return (
        <LoginContainer title={I18n.t('expired.title')}>
            <ErrorAlert>{I18n.t('expired.info')}</ErrorAlert>
            {homeUrl && (
                <Button
                    type="button"
                    onClick={() => {
                        window.location.href = homeUrl;
                    }}
                >
                    {I18n.t('expired.back')}
                </Button>
            )}
        </LoginContainer>
    );
}
