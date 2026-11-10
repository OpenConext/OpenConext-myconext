import './EnvironmentRibbon.scss';

export const ENVIRONMENT_RIBBON_URL = '/myconext/api/disclaimer';

// The server only supplies `background` and `content` of `body::after`, the
// position lives in EnvironmentRibbon.scss. React hoists the link into the
// head because of `precedence`.
export const EnvironmentRibbon = () => (
    <link rel="stylesheet" href={ENVIRONMENT_RIBBON_URL} precedence="default" />
);
