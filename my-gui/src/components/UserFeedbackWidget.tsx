import { UserFeedbackWidget as SharedUserFeedbackWidget } from '@myconext/shared-gui';

import { sendFeedback } from '../api';
import I18n from '../locale/I18n';
import { useConfigStore } from '../store/config';
import { useUserStore } from '../store/me';

export const UserFeedbackWidget = () => {
    const user = useUserStore((state) => state.user);
    const config = useConfigStore((state) => state.config);

    return (
        <SharedUserFeedbackWidget
            user={user}
            config={config}
            t={(key, options) => I18n.t(key, options)}
            locale={I18n.locale}
            sendFeedback={sendFeedback}
        />
    );
};
