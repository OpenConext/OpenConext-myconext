const en = {
    code: 'EN',
    name: 'English',
    languages: {
        language: 'Language',
        en: 'English',
        nl: 'Dutch',
    },
    landing: {
        header: {
            title: 'eduID Dashboard',
            subTitle: 'Hello {{name}}',
            login: 'Log in',
            sup: 'eduID Dashboard is by invitation only.',
        },
        works: 'How does it work?',
        adminFunction: 'Employee',
        studentFunction: 'Student',
        info: [
            //Arrays of titles and info blocks and if a function is an admin function
            [
                'Security',
                '<p>eduID Dashboard is a secure platform designed to validate the identity of students who have applied for an identity check. This service ensures compliance with formal identification standards while providing a user-friendly process for employees and students.</p>',
                true,
            ],
            [
                'Compliance',
                '<p>Built to adhere to formal identification regulations and ensure accuracy in identity verification. Reduces errors through a structured and step-by-step validation process</p>',
                true,
            ],
            [
                'Authentication',
                '<p>Employees initiate the verification process using a unique code sent to the student via email. The system ensures that only students with valid application codes can proceed with the identity check.</p><br/>',
                false,
            ],
        ],
        footer:
            '<p>The eduID Dashboard simplifies the identity verification process, ensuring security and efficiency.</p>' +
            "<p>Do you want to know more? <a href='https://support.surfconext.nl/servicedesk-en'>Read more</a>.</p>",
    },
    header: {
        title: 'eduID Dashboard',
        links: {
            logout: 'Logout',
        },
    },
    serviceDesk: {
        member: 'Dashboard member',
    },
    navigation: {
        overview: 'Overview',
        users: 'Users',
        identityCheck: 'Identity check',
    },
    overview: {
        accountsOverTime: 'Accounts over time',
        total: 'Total accounts',
        added: 'New accounts',
        services: 'Accounts per service',
        servicesSubtitle: 'Services the eduID accounts are logged into',
        searchServices: 'Search services…',
        clearSearch: 'Clear search',
        showMore: 'Show more (+%{count})',
        showLess: 'Show less',
        noData: 'No data available',
        loadError: 'The overview could not be loaded',
    },
    users: {
        title: 'Users',
        description:
            'Search eduID accounts with an active link or invite role for your institution.',
        search: 'Search by name, email address or id…',
        mfaFilter: 'MFA methods',
        allUsers: 'All users',
        blockedFilter: 'Blocked',
        blocked: 'Blocked',
        notBlocked: 'Not blocked',
        columns: {
            name: 'Name',
            email: 'Email address',
            mfa: 'MFA method',
            affiliations: 'Affiliation(s)',
            id: 'ID',
        },
        mfa: {
            app: 'Mobile app',
            passkey: 'Passkey',
            password: 'Password',
        },
        affiliation: {
            student: 'Student',
            employee: 'Employee',
            staff: 'Employee',
            affiliate: 'Affiliate',
            member: 'Member',
            faculty: 'Faculty',
            alum: 'Alumnus',
        },
        blockedUser: 'Blocked',
        previous: 'Previous',
        next: 'Next',
        morePages: 'More pages',
        summary: '{{from}} to {{to}} of {{total}} users',
        empty: 'No users found',
        error: 'The users could not be retrieved',
    },
    userDetail: {
        back: 'Back to users',
        notFound: 'This user could not be found',
        block: 'Block',
        verifiedWith: 'Verified with {{issuer}}',
        givenName: 'First name',
        familyName: 'Last name',
        chosenName: 'Nickname',
        dateOfBirth: 'Date of birth',
        id: 'ID',
        eppn: 'ePPN',
        mfa: 'MFA method',
        noMfa: 'None',
        reset: 'Reset',
        loginCode: 'Send a login code to',
        change: 'Change',
        verified: 'Verified',
        blocked: 'Blocked',
    },
    activity: {
        title: 'Activity',
        filter: 'Filter by type',
        allTypes: 'All types',
        empty: 'No activity yet',
        showMore: 'Show more',
        types: {
            account: 'Account',
            security: 'Security',
            institution: 'Institution',
            service: 'Service',
        },
        events: {
            ACCOUNT_CREATED: 'Account created via {{institution}}',
            NAME_UPDATED: 'Name changed to {{name}}',
            LANGUAGE_CHANGED: 'Language changed to {{language}}',
            EMAIL_CHANGED: 'Email address changed to {{email}}',
            PASSWORD_ADDED: 'Password added',
            PASSWORD_UPDATED: 'Password changed',
            PASSWORD_DELETED: 'Password removed',
            PASSKEY_ADDED: 'Passkey {{name}} added',
            PASSKEY_RENAMED: 'Passkey renamed to {{name}}',
            PASSKEY_DELETED: 'Passkey {{name}} removed',
            APP_ACTIVATED: {
                default: 'eduID app activated',
                phone: 'eduID app activated with a phone number',
                recoveryCode: 'eduID app activated with a recovery code',
            },
            APP_DEACTIVATED: 'eduID app deactivated',
            RECOVERY_METHOD_CHANGED: {
                default: 'Recovery method changed',
                phone: 'Recovery method changed to a phone number',
                recoveryCode: 'Recovery method changed to a recovery code',
            },
            INSTITUTION_LINKED: 'Institution {{institution}} linked',
            INSTITUTION_REMOVED: 'Institution {{institution}} removed',
            PREFERRED_INSTITUTION_CHANGED:
                'Preferred institution changed to {{institution}}',
            IDENTITY_VERIFIED: 'Identity verified by {{institution}}',
            IDENTITY_REMOVED: 'Verified identity from {{institution}} removed',
            CONTROL_CODE_CREATED: 'Service desk control code requested',
            CONTROL_CODE_DELETED: 'Service desk control code removed',
            SERVICE_REMOVED: 'Login details of {{service}} removed',
            TOKENS_REMOVED: 'Access of a service revoked',
            SERVICE_FIRST_LOGIN: 'First login at {{service}}',
            SERVICE_LAST_LOGIN: 'Latest login at {{service}}',
        },
    },
    issuers: {
        idin: 'iDIN',
        eherkenning: 'eIDAS',
        studielink: 'Studielink',
        serviceDesk: 'Service desk',
        servicedesk: 'Service desk',
    },
    blockUser: {
        title: 'Block user?',
        question:
            'Are you sure you want to block the access of the eduID of {{name}} at {{institution}}?',
        info: {
            institution:
                'The eduID can no longer log in to services of {{institution}}.',
            active: 'The eduID itself remains active and usable for services of other institutions.',
            indefinite:
                'The block applies indefinitely; you can always lift it later.',
            notice: '{{name}} will not receive an automatic message about this block.',
        },
        cancel: 'Cancel',
        confirm: 'Block',
        error: 'The user could not be blocked',
    },
    identityCheck: {
        title: 'Identity check',
        codeTitle: 'Enter the check code',
    },
    info: {
        title: "Verify someone's identity",
        subTitle:
            'An eduID user has made an appointment to have their identity verified.',
        verify1: 'Ask for <strong>the eduID verification code</strong>',
        verify2:
            'This code should already be generated by the user in the eduID application.',
        verify3:
            'Does the user not have a code, ask the user to make one on the spot by starting the verification process in eduID.',
    },
    verification: {
        header: 'Enter the eduID verification code',
        proceed: 'Continue',
        error: 'No user found for verification code {{code}}',
        info: 'The person generated this code in eduID.',
        flash: 'Verification code found for user {{name}}',
    },
    control: {
        forCode: 'For code',
        restart: 'Start again',
        proceed: 'Confirm',
        error: "An unexpected error has occurred. Please contact <a href='mailto:servicedesk@surf.nl?subject=Reference {{reference}}'>eduID Dashboard</a>",
        info: 'Check this person’s ID',
        validDocuments:
            'Valid documents: passports, EEA ID cards, Dutch driving licenses and Dutch residence permits',
        inValidDocuments:
            'Copies, public transport tickets and student cards are not valid documents',
        isValid: 'Is everything correct?',
        validations: {
            photo: 'The person <span>looks like the photo</span> in the document',
            valid: 'The document is <span>still valid</span>',
            lastName:
                'The last name in the document: <span>{{lastName}}</span>',
            firstName:
                'The first names in the document: <span>{{firstName}}</span>',
            dayOfBirth:
                'The date of birth in the document: <span>{{dayOfBirth}}</span> ',
        },
        invalidDate:
            'The day of birth has an invalid format, first select the correct date by pressing the calendar button at the right',
        validDate: 'The day of birth is now valid and can be confirmed',
        idDocument:
            'Fill in the <span>last 6 characters</span> of the ID document',
    },
    forbidden: {
        info: 'Unfortunately you do not have access to the ServiceDesk. If you logout, you can try again with a different account.',
        logout: 'Logout',
    },
    confirmation: {
        header: 'The identity of the person has been confirmed',
        info: 'From now on, the person will see a check mark in front of the personal data in My eduID.',
        viewEduId: 'View eduID',
        restart: 'Start a new check',
    },
    footer: {
        terms: 'Terms of Use',
        termsLink: 'https://support.surfconext.nl/terms-en',
        privacy: 'Privacy policy',
        privacyLink: 'https://support.surfconext.nl/privacy-en',
        surfLink: 'https://surf.nl',
        select_locale: 'Select your preferred language',
    },
};

export default en;
