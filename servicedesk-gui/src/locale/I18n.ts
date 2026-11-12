import { createI18n } from '@myconext/shared-gui';

import en from './en';
import nl from './nl';

export const { I18n, AVAILABLE_LANGUAGES, switchLocale } = createI18n({
    translations: { en, nl },
});

export default I18n;
