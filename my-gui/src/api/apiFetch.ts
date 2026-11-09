import { createApiFetch } from '@myconext/shared-gui';

import I18n from '../locale/I18n';

export const apiFetch = createApiFetch({
    getLocale: () => I18n.locale,
    reloadOnSessionLoss: true,
});
