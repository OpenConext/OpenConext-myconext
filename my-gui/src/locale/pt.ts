import type { AppTranslation } from './translationType';

const pt: AppTranslation = {
    globalVariables: {
        productName: 'eduID',
    },
    code: 'PT',
    languages: {
        language: 'Language',
        en: 'English',
        nl: 'Dutch',
        pt: 'Portuguese',
    },
    common: {
        loading: 'Loading...',
        error: 'Something went wrong',
    },
    logo: {
        eduid: 'eduID',
        surf: 'SURF',
    },
    navigation: {
        home: 'Home',
        identity: 'Identity',
        institutions: 'Institutions',
        security: 'Security',
        activity: 'Activity',
    },
    userMenu: {
        account: 'Account',
        darkMode: 'Dark mode',
        lightMode: 'Light mode',
        logout: 'Log out',
    },
    footer: {
        privacy: 'Privacy & terms',
        privacyUrl: 'https://eduid.nl/privacy-policy/',
        about: 'About eduID',
        aboutUrl: 'https://eduid.nl',
    },
    home: {
        title: 'Welcome {{name}}',
        lead: 'Here you can view and edit your details, decide how you log in and manage which institutions and services your eduID is linked to.',
        intro: {
            title: 'What is eduID?',
            description:
                'eduID is your personal account for logging in to education and research institutions in the Netherlands, whether you are a student, employee or professional. Your eduID stays yours, even if you change institution or job.',
            dismiss: 'Understood, hide',
        },
        cards: {
            identity: {
                title: 'Identity',
                unverified: 'Not yet verified',
                verified: 'Verified',
            },
            institutions: {
                title: 'Institutions',
                none: 'No connection yet',
                count: {
                    one: '{{count}} connection',
                    other: '{{count}} connections',
                },
            },
            security: {
                title: 'Security',
                none: 'No login method set up',
                count: {
                    one: 'One login method set up',
                    other: '{{count}} login methods set up',
                },
            },
            activity: {
                title: 'Activity',
            },
        },
        activity: {
            empty: 'No activity yet',
            title: 'Recent activity',
            viewAll: 'View all activity',
        },
    },
    activity: {
        all: 'All activity',
        events: {
            ACCOUNT_CREATED: 'Account created',
            NAME_UPDATED: 'Name changed',
            LANGUAGE_CHANGED: 'Language changed',
            EMAIL_CHANGED: 'Email address changed',
            PASSWORD_ADDED: 'Password added',
            PASSWORD_UPDATED: 'Password changed',
            PASSWORD_DELETED: 'Password removed',
            PASSKEY_ADDED: 'Passkey added',
            PASSKEY_RENAMED: 'Passkey renamed',
            PASSKEY_DELETED: 'Passkey removed',
            APP_ACTIVATED: 'eduID app activated',
            APP_DEACTIVATED: 'eduID app deactivated',
            RECOVERY_METHOD_CHANGED: 'Recovery method changed',
            INSTITUTION_LINKED: 'Institution linked',
            INSTITUTION_REMOVED: 'Institution removed',
            PREFERRED_INSTITUTION_CHANGED: 'Preferred institution changed',
            IDENTITY_VERIFIED: 'Identity verified',
            IDENTITY_REMOVED: 'Verified identity removed',
            CONTROL_CODE_CREATED: 'Service desk control code requested',
            CONTROL_CODE_DELETED: 'Service desk control code removed',
            SERVICE_REMOVED: 'Login details of a service removed',
            TOKENS_REMOVED: 'Access of a service revoked',
        },
    },
    pages: {
        identity: 'Identity',
        identityEditEmail: 'Edit email address',
        identityEppnAlreadyLinked: 'Institution already linked',
        identityAttributeMissing: 'Attribute missing',
        identitySubjectAlreadyLinked: 'Account already linked',
        identityExternalAccountLinkedError: 'Linking the account failed',
        institutions: 'Institutions',
        security: 'Security',
        securityCredential: 'Passkey',
        securityWebauthn: 'Add a passkey',
        securityPassword: 'Password',
        securityApp: 'eduID app',
        activity: 'Activity',
        account: 'Account',
        accountDelete: 'Delete account',
        updateEmail: 'Confirm email address',
        installApp: 'Install the eduID app',
        createFromInstitution: 'Create eduID from institution',
        createFromInstitutionStep: 'Create eduID from institution',
    },
    landing: {
        logout: 'You are logged out',
        delete: 'Your account has been deleted',
        ratelimit: 'Too many attempts',
        none: 'Page not found',
    },
    notFound: {
        title: '404 - Page not found',
    },
};

export default pt;
