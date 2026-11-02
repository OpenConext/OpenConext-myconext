import { Button } from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { fetchConfig } from '../../api';
import { LoginContainer } from '../../components';
import I18n from '../../locale/I18n.ts';

export default function Expired() {
    const { data: config, isPending } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });

    if (isPending) {
        return null;
    }

    const homeUrl = config?.baseDomain ? `https://${config.baseDomain}` : null;

    return (
        <LoginContainer title={I18n.t('expired.title')}>
            <p role="alert">{I18n.t('expired.info')}</p>
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
