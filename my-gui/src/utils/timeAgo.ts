import { format, register } from 'timeago.js';
import nl from 'timeago.js/lib/lang/nl';
import pt from 'timeago.js/lib/lang/pt_BR';

import I18n from '../locale/I18n';

register('nl', nl);
register('pt', pt);

// e.g. `just now` or `2 weeks ago`, in the language of the user
export const timeAgo = (date: number | string): string =>
    format(date, I18n.locale);
