import { StoreBadges as SharedStoreBadges } from '@myconext/shared-gui';

import I18n from '../locale/I18n';

export const StoreBadges = () => (
    <SharedStoreBadges
        googleUrl={I18n.t('app.get.google')}
        appleUrl={I18n.t('app.get.apple')}
        googleLabel={I18n.t('app.get.googleLabel')}
        appleLabel={I18n.t('app.get.appleLabel')}
    />
);
