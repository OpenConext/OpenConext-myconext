import { UserFeedbackWidget as SharedUserFeedbackWidget } from '@myconext/shared-gui';
import { useQuery } from '@tanstack/react-query';

import { fetchConfig, fetchMe, sendFeedback } from '../api';
import I18n from '../locale/I18n';

export const UserFeedbackWidget = () => {
    const { data: user } = useQuery({ queryKey: ['me'], queryFn: fetchMe });
    const { data: config } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });

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
