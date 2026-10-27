import { createI18n } from '@myconext/shared-gui';

import { enOverride, nlOverride } from '../appCustomizations/';
import en from './en';
import nl from './nl';

export const { I18n, AVAILABLE_LANGUAGES, switchLocale } = createI18n({
    translations: { en, nl },
    overrides: { en: enOverride, nl: nlOverride },
});

export default I18n;
