import I18n from "i18n-js";

I18n.translations.en = {
    login: {
        requestEduId: "No eduID?",
        requestEduId2: "Request one!",
        loginEduId: "Login!",
        whatis: "What is eduID?",
        header: "Sign in with eduID",
        headerSubTitle: "to continue to ",
        header2: "Request your eduID",
        trust: "Trust this computer",
        loginOptions: "Other sign-in options",
        loginOptionsToolTip: "We offer 3 ways to sign-in:</br><ol>" +
            "<li>We can send you a magic link to your email.</li>" +
            "<li>You can use your own password, you need to set this up first within My eduID.</li>" +
            "<li>You can use a security key, you need to register this key first within My eduID.</li>" +
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
        emailNotFound3: "request a new eduID account.",
        emailOrPasswordIncorrect: "Email or password are incorrect",
        institutionDomainNameWarning: "It looks like you entered an institutional email address. Please note that when you no longer study at or work for that institution, you can no longer use that email address.",
        institutionDomainNameWarning2: "We recommend using your personal email address for eduID.",
        allowedDomainNamesError: "Domain name {{domain}} not allowed.",
        allowedDomainNamesError2: "eduID is restricted to be used only for allowed domains.",
        passwordDisclaimer: "Make sure it's at least 15 characters long OR at least 8 characters when including a number and an UpperCase letter.",
        alreadyGuestAccount: "Already have an eduID?",
        usePasswordLink: "Type a password anyway",
        useWebAuthnLink: "Or use WebAuthn",
        agreeWithTerms: "<span>I agree with <a href='https://eduid.nl/terms_of_service/' target='_blank'>the terms of service.</a> I also understand <a href='https://eduid.nl/privacy_policy/' target='_blank'>the privacy policy</a>.</span>"
    },
    magicLink: {
        header: "Check your email!",
        info: "To sign in, click the link in the email we sent to <strong>{{email}}</strong>.",
        awaiting: "Waiting for you to click the link...",
        openGMail: "Open Gmail.com",
        openOutlook: "Open Outlook.com",
        spam: "Can't find it? Check your spam folder.",
        loggedIn: "Login succeeded!",
        loggedInInfo: "You can close this tab / window.",
        timeOutReached: "Timeout!",
        timeOutReachedInfo: "Your link has expired. Please go back to the service you where heading to and re-try again.",
        resend: "Still can't find it?",
        resendLink: " Send me the mail again.",
        mailResend: "Check your inbox again. We have sent the mail with the magic link again.",
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
        info: "To proceed to <strong> {{name}} </strong>, you must meet the following condition(s).",
        link: "Verify this via SURFconext"
    },
    footer: {
        privacy: "Privacy policy",
        terms: "Terms of Use",
        help: "Help",
        poweredBy: "Powered by"
    },
    success: {
        title: "Login succeeded!",
        info: "Please go back to the screen where you have requested the magic link.<br/><br/>You can close this tab / window."
    },
    expired: {
        title: "Expired magic link",
        info: "The magic link you have used is either expired or has already been used.",
        back: "Go to eduid.nl"
    },
    notFound: {
        title: "Whoops...",
        title2: "Something went wrong (404)"
    },
    webAuthn: {
        info: "Enable Public Key Cryptography and Web Authentication (WebAuthn)",
        browserPrompt: "Your browser is prompting you to register one of your security keys or fingerprint with your account"
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
        proceed: "You can try to link to another institution or proceed to {{name}}",
        proceedLink: "Proceed",
        retryLink: "Retry"
    },
    validNameMissing: {
        header: "Account linked, but...",
        info: "Your eduID is successfully linked, however the institution you choose did not provide a valid name.",
        proceed: "You can try to link to another institution or proceed to {{name}}",
        proceedLink: "Proceed",
        retryLink: "Retry"
    },
    eppnAlreadyLinked: {
        header: "Account not linked!",
        info: "Your eduID could not be linked, because the trusted account with which you logged in, is already linked to a different eduID account.",
        proceed: "You can try to link to another institution or proceed to {{name}}",
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
        affiliation_student: "You have proven that you are following education by linking your eduID account to a trusted party.."
    }

};
