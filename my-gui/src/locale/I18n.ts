import { createI18n } from '@myconext/shared-gui';

import { enOverride, nlOverride, ptOverride } from '../appCustomizations/';
import en from './en';
import nl from './nl';
import pt from './pt';

export const { I18n, AVAILABLE_LANGUAGES, switchLocale } = createI18n({
    translations: { en, nl, pt },
    overrides: { en: enOverride, nl: nlOverride, pt: ptOverride },
});

export default I18n;
