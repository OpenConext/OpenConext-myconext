import I18n from "i18n-js";

I18n.translations.en = {
    login: {
        requestEduId: "No eduID?",
        requestEduId2: "Create one!",
        loginEduId: "Login!",
        whatis: "What is eduID?",
        header: "Sign in with eduID",
        headerSubTitle: "to continue to ",
        header2: "Request your eduID",
        trust: "Trust this computer",
        loginOptions: "Other sign-in options",
        loginOptionsToolTip: "We offer 3 ways to sign-in:</br><ol>" +
            "<li>You can receive a magic link sent to your email address.</li>" +
            "<li>You can use a password. You must first set this up in My eduID.</li>" +
            "<li>You can use a security key. You must first set this up in My eduID.</li>" +
            "</ol>",
        email: "Your email address",
        emailPlaceholder: "e.g. user@gmail.com",
        passwordPlaceholder: "Password",
        familyName: "Last name",
        givenName: "First name",
        familyNamePlaceholder: "e.g. Berners-Lee",
        givenNamePlaceholder: "e.g. Tim",
        sendMagicLink: "Email a magic link",
        loginWebAuthn: "Login with security key",
        usePassword: "type a password.",
        usePasswordNoWebAuthn: "Type a password.",
        useMagicLink: "Email a magic link",
        useMagicLinkNoWebAuthn: "Email a magic link.",
        useWebAuth: "Sign in with a security key",
        useOr: "or",
        requestEduIdButton: "Request your eduID",
        rememberMe: "Stay logged in",
        password: "Your password",
        passwordForgotten: "Forgot your password or prefer a magic link? ",
        passwordForgottenLink: "Receive an email to sign in instantly.",
        login: "Login",
        create: "Create",
        newTo: "New to eduID?",
        createAccount: " Create an account.",
        useExistingAccount: "Use existing account",
        invalidEmail: "Invalid email",
        requiredAttribute: "{{attr}} is required",
        emailInUse1: "This email is already in use.",
        emailInUse2: "Try another, or ",
        emailInUse3: " login with this eduID account.",
        emailNotFound1: "We could not find an eduID with that email.",
        emailNotFound2: "Try another, or ",
        emailNotFound3: "create a new eduID account.",
        emailOrPasswordIncorrect: "Email or password are incorrect",
        institutionDomainNameWarning: "It looks like you entered an institutional email address. Please note that when you no longer study at or work for that institution, you can no longer use that email address.",
        institutionDomainNameWarning2: "We recommend using your personal email address for eduID.",
        allowedDomainNamesError: "Domain name {{domain}} not allowed.",
        allowedDomainNamesError2: "eduID is restricted to be used only for allowed domains.",
        passwordDisclaimer: "Make sure it's at least 15 characters long OR at least 8 characters when including a number and an UpperCase letter.",
        alreadyGuestAccount: "Already have an eduID?",
        usePasswordLink: "Type a password anyway",
        useWebAuthnLink: "Or use a security key",
        agreeWithTerms: "<span>I agree with <a tabindex='-1' href='https://eduid.nl/terms-of-use/' target='_blank'>the terms of service.</a> I also understand <a tabindex='-1' href='https://eduid.nl/privacy_policy/' target='_blank'>the privacy policy</a>.</span>",
        next: "Next",
        useOtherAccount: "Use another account",
        noAppAccess: "No access to your app?",
        noMailAccess: "No access to your mail?",
        forgotPassword: "Forgot your password?",
        useAnother: "Use another",
        optionsLink: "sign-in option.",
    },
    options: {
        header: "How do you want to login?",
        noLogin: "Still not able to login?",
        learn: "Learn how to",
        learnLink: "recover your acccount",
        useApp: "Use the <strong>eduID app</strong> to sign in with your mobile device.",
        useWebAuthn: "Use your <strong>security key</strong>.",
        useLink: "Get a <strong>magic link</strong> sent to your inbox.",
        usePassword: "Use <strong>a password</strong>.",
    },
    magicLink: {
        header: "Check your email!",
        info: "To sign in, click the link in the email we sent to <strong>{{email}}</strong>.",
        awaiting: "Waiting for you to click the link...",
        openGMail: "Open Gmail.com",
        openOutlook: "Open Outlook.com",
        spam: "Can't find the email? Check your spam folder.",
        loggedIn: "Login succeeded!",
        loggedInInfo: "You can close this tab / window.",
        timeOutReached: "Timeout!",
        timeOutReachedInfo: "Your link has expired. Please go back to the service you where heading to and try again.",
        resend: "Still can't find the email?",
        resendLink: "Send the email again.",
        mailResend: "Check your inbox again. We've sent another email with a magic link.",
        loggedInDifferentDevice: "Verification code required",
        loggedInDifferentDeviceInInfo: "The magic link we sent you opened in a different browser than the one used to request the magic link. As an extra security measure you must enter a verification code.",
        loggedInDifferentDeviceInInfo2: "We sent you an extra email with the verification code.",
        verificationCodeError: "Wrong verification code.",
        verify: "Verify"
    },
    confirm: {
        header: "Success!",
        thanks: "Your eduID account has been created. Proceed to your destination.",
    },
    confirmStepup: {
        header: "Thanks!",
        proceed: "Go to {{name}}",
        conditionMet: "All conditions are met."
    },
    stepup: {
        header: "One more thing!",
        info: "To proceed to <strong>{{name}}</strong>, you must meet the following condition(s).",
        link: "Verify this via SURFconext",
        linkExternalValidation: "Verify this via ReadID"
    },
    footer: {
        privacy: "Privacy policy",
        terms: "Terms of Use",
        help: "Help",
        poweredBy: "Powered by"
    },
    success: {
        title: "Login almost done!",
        info: "Please go back to the screen where you requested the magic link and follow the instructions there.<br/><br/>You can close this tab / window."
    },
    expired: {
        title: "Expired magic link",
        info: "The magic link you used is either expired or has already been used.",
        back: "Go to eduid.nl"
    },
    maxAttempt:{
        title: "Maximum attempts reached",
        info: "You've reached the maximum verification attempts.",
    },
    notFound: {
        title: "Whoops...",
        title2: "Something went wrong (404)"
    },
    webAuthn: {
        info: "Add security key",
        browserPrompt: "Click the button below to add a security key to your eduID account. Please follow the instructions given by your browser.",
        start: "Start",
        header: "Sign in with a security key",
        explanation: "Your device will open a security window. Follow the instructions there to sign in.",
        next: "Log in with a security key",
        error: "Currently you can not use your security key to login."
    },
    useLink: {
        header: "Request a magic link",
        next: "Email a magic link"
    },
    usePassword: {
        header: "Enter your password",
        passwordIncorrect: "Password is incorrect"
    },
    webAuthnTest: {
        info: "Test security key",
        browserPrompt: "Click the button below to test a security key. Please follow the instructions given by your browser.",
        start: "Test"
    },
    migration: {
        header: "Migrate to an <br/>eduID guest account",
        info1: "SURF will phase out the use of Onegini. To retain access, you must migrate your Onegini account to an eduID account.",
        info2: "You only need to click the button and log in with your existing Onegini account once. We will then migrate your account to eduID and send you an email after completion.",
        link: "Start migration"
    },
    affiliationMissing: {
        header: "Account linked, but...",
        info: "Your eduID is successfully linked, however the institution you choose did not provide the correct affiliation.",
        proceed: "You can try to link to another institution or proceed to {{name}}.",
        proceedLink: "Proceed",
        retryLink: "Retry"
    },
    eppnAlreadyLinked: {
        header: "Account not linked!",
        info: "Your eduID could not be linked. The trusted account with which you just logged in, is already linked to a different eduID account: {{email}}.",
        proceed: "You can try to link to another institution or proceed to {{name}}.",
        proceedLink: "Proceed",
        retryLink: "Retry"
    },
    validNameMissing: {
        header: "Account linked, but...",
        info: "Your eduID is successfully linked, however the institution you choose did not provide a valid name.",
        proceed: "You can try to link to another institution or proceed to {{name}}.",
        proceedLink: "Proceed",
        retryLink: "Retry"
    },
    stepUpExplanation: {
        linked_institution: "Your eduID account must be linked to a trusted party.",
        validate_names: "Your first name and last name must be verified by a trusted party.",
        affiliation_student: "You must prove that you are following education by linking your eduID account to a trusted party."
    },
    stepUpVerification: {
        linked_institution: "Your eduID account is linked to a trusted party.",
        validate_names: "Your first name and last name are verified by a trusted party.",
        affiliation_student: "You have proven that you are following education by linking your eduID account to a trusted party."
    }

};
