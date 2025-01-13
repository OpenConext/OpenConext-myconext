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
        emailForbidden: "The creation of an eduID account for this email-domain is denied, please contact <a href=\"mailto:help@eduid.nl\">help@eduid.nl</a> if you think this email-domain is valid.",
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
        sendingEmail: "Sending email..."
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
        info: "To sign in, click the link in the email we sent to ",
        awaiting: "Waiting for you to click the link...",
        openGMail: "Open Gmail.com",
        openOutlook: "Open Outlook.com",
        spam: "Can't find the email? Check your spam folder.",
        loggedIn: "Login succeeded!",
        loggedInInfo: "You can close this tab / window.",
        timeOutReached: "Timeout!",
        timeOutReachedInfo: "Your link has expired. Please go back to the application you where heading to and try again.",
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
        link: "Verify now"
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
    redirectMobileApp: {
        "account-linked": {
            title: "Account linked!",
            info: "Your institutional account has been linked, click the button below to open your new account in the eduID app.",
        },
        "add-password": {
            title: "New password requested!",
            info: "To add your new password, click the button below to open eduID app.",
        },
        created: {
            title: "Account created!",
            info: "Your account has been created, click the button below to open your new account in the eduID app.",
        },
        "eppn-already-linked": {
            title: "EPPN already linked!",
            info: "Your account has not been linked, click the button below to open the eduID app.",
        },
        "subject-already-linked": {
            title: "Subject already linked!",
            info: "Your account has not been verified, click the button below to open the eduID app.",
        },
        expired: {
            title: "Session expired!",
            info: "Your account has not been linked, click the button below to open the eduID app.",
        },
        fallback: {
            title: "eduID app!",
            info: "You have opened a link to the eduID app in the browser, click the button below to open eduID app."
        },
        "reset-password": {
            title: "Reset password requested!",
            info: "To reset your password, click the button below to open eduID app.",
        },
        "security": {
            title: "Security!",
            info: "To view and update your security settings, click the button below to open eduID app.",
        },
        "update-email": {
            title: "New email confirmed!",
            info: "To finalize your email update, click the button below to open eduID app.",
        },
        "external-account-linked": {
            title: "External Account linked!",
            info: "Your external account has been linked, click the button below to open your new account in the eduID app.",
        },
        "external-account-linked-error": {
            title: "External account error!",
            info: "Your external account has not been verified, click the button below to open the eduID app.",
        },
        proceedLink: "Go to eduID app"
    },
    expired: {
        title: "Expired magic link",
        info: "The magic link you used is either expired or has already been used.",
        back: "Go to eduid.nl"
    },
    maxAttempt: {
        title: "Maximum attempts reached",
        info: "You've reached the maximum verification attempts. Please return to the application you want to log in to and try again.",
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
    useApp: {
        header: "Check your eduID app",
        info: "We have sent a push-notification to your app, to verify it's you trying to sign in.",
        scan: "Scan this QR code with your eduID app",
        noNotification: "No notification?",
        qrCodeLink: "Create a QR-code",
        qrCodePostfix: "and scan it.",
        offline: "When your device is offline, you must enter a",
        offlineLink: "one time code.",
        openEduIDApp: "Open the app on this device",
        lost: "Lost your app?",
        lostLink: "Learn <a href=\"https://eduid.nl/help\" target=\"_blank\">how to register a new one</a>.",
        timeOut: "Session timeout",
        timeOutInfoFirst: "Your session timed out. Click this ",
        timeOutInfoLast: " to try again.",
        timeOutInfoLink: "link",
        existingRegistration: "Existing registration",
        existingRegistrationInfoFirst: "You can't start a new registration as you already have a registration. Click this ",
        existingRegistrationInfoLast: " to try to login again.",
        existingRegistrationInfoLink: "link",
        responseIncorrect: "The response is invalid.",
        suspendedResult: "The verification from your eduID app failed. ",
        accountNotSuspended: "You can try again.",
        accountSuspended: "You'll have to wait {{minutes}} {{plural}} before you can try again.",
        minutes: "minutes",
        minute: "minute"
    },
    enrollApp: {
        header: "Finish setup in the eduID app"
    },
    recovery: {
        header: "Set up a recovery method",
        info: "If you can't access eduID with the app or via email, you can use a recovery method to sign in to your eduID account.",
        methods: "The following methods are available.",
        phoneNumber: "Add a recovery phone number.",
        phoneNumberInfo: "You'll receive a text message with a code.",
        backupCode: "Request a recovery code.",
        backupCodeInfo: "The code can be used to sign in with.",
        save: "Save the code somewhere safe.",
        active: "This code is active now, but you can generate a new code within My-eduID anytime.",
        copy: "Copy the code",
        copied: "Copied",
        continue: "My code is safe. Continue",
        leaveConfirmation: "Are you sure you want to leave the page? Changes will not be saved."
    },
    phoneVerification: {
        header: "Add a recovery phone number",
        info: "Your phone number will be used for security purposes, such as helping you get back into your account if you ever lose your app",
        text: "We will text you a code to verify your number",
        verify: "Verify this phone number",
        placeHolder: "+31 612345678",
        phoneIncorrect: "Phone number is incorrect"
    },
    congrats: {
        header: "Success",
        info: "You can now use the eduID app to quickly login to applications which require you to login with your eduID.",
        next: "Onwards to {{name}}"
    },
    webAuthnTest: {
        info: "Test security key",
        browserPrompt: "Click the button below to test a security key. Please follow the instructions given by your browser.",
        start: "Test"
    },
    affiliationMissing: {
        header: "Account linked, but...",
        info: "Your eduID is successfully linked, however the institution you choose did not provide an affiliation.",
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
    attributeMissing: {
        header: "Account not linked!",
        info: "Your eduID could not be linked. The trusted account with which you just logged in, did not provide and EPPN.",
        proceed: "You can try to link to another institution or proceed to {{name}}.",
        proceedLink: "Proceed",
        retryLink: "Retry"
    },
    subjectAlreadyLinked: {
        header: "Account not verified!",
        info: "Your eduID could not be verified. The external account with which you just logged in, is already linked to a different eduID account.",
        proceed: "You can try to verify your account again or proceed to {{name}}.",
        proceedLink: "Proceed",
        retryLink: "Retry"
    },
    externalAccountLinkedError: {
        header: "Account not verified!",
        info: "Your eduID could not be verified. The trusted party you selected returned an error."
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
        validate_names_external: "Your identity must be verified by a trusted party.",
        affiliation_student: "You must prove that you are following education by linking your eduID account to a trusted party."
    },
    stepUpVerification: {
        linked_institution: "Your eduID account is linked to a trusted party.",
        validate_names: "Your first name and last name are verified by a trusted party.",
        validate_names_external: "Your identity is verified by a trusted party.",
        affiliation_student: "You have proven that you are following education by linking your eduID account to a trusted party."
    },
    nudgeApp: {
        new: "Your eduID account has been created!",
        header: "Want to sign in quicker and more secure next time?",
        info: "Get the <strong>eduID app</strong> and securely sign in without passwords or accessing your email. It will only take a minute.",
        no: "No thanks",
        noLink: "/proceed",
        yes: "Get it now",
        yesLink: "/eduid-app"
    },
    getApp: {
        header: "Download the eduID app",
        info: "Download and install <a href=\"https://eduid.nl/help\" target=\"_blank\">the eduID app</a> (issued by SURF) on your mobile device.",
        google: "https://play.google.com/store/apps/details?id=nl.eduid",
        apple: "https://apps.apple.com/nl/app/eduid/id1600756434",
        after: "When you've downloaded the eduID app on your phone, come back here and click next.",
        back: "Back",
        next: "Next"
    },
    sms: {
        header: "Check your phone",
        info: "Enter the six-digit code we sent to your phone to continue",
        codeIncorrect: "The code is incorrect",
        sendSMSAgain: "Resend code",
        maxAttemptsPre: "Max attempts reached. Click",
        maxAttemptsPost: "to use a different phone number or resend code to receive a new verification",
        here: " here "
    },
    rememberMe: {
        yes: "Yes",
        no: "No",
        header: "Stay signed in?",
        info: "Stay signed in on this device so you don't have to sign in next time."
    },
    modal: {
        cancel: "Cancel",
        confirm: "Confirm",
    },
    appRequired: {
        header: "Login with the eduID app",
        info: "Login with the eduID app to ensure your identity.",
        info2: "Get the <strong>eduID app</strong> and securely sign in without passwords or accessing your email. It will only take a minute. Please click <strong>Proceed</strong> for the next step.",
        cancel: "/cancel",
        no: "I refuse",
        yesLink: "/proceed",
        yes: "Proceed",
        warning: "Login without the eduID app is not allowed. You will not be able to log in to {{service}}.",
        warningTitle: "Get the eduID app",
        confirmLabel: "Get the eduID app",
        cancelLabel: "I still refuse"
    },
    subContent: {
        warningTitle: "Please reconsider",
        warning: "The application has requested you login using your eduID app. If you login with a different method, this application will not receive your personal data.",
        confirmLabel: "Change login option anyway",
        cancelLabel: "Did not know that"
    },
    verify: {
        modal: {
            header: "Verification",
            info: {
                title: "Verification",
                verify: "Verify your identity, quick and easy",
                please: "Please verify that you are who you say you are.",
                educationalInstitution: "Verify via a Dutch educational institution",
                selectInstitution: "Select your institution",
                other: "Other options...",
                verifyBank: "Verify with a Dutch bank app",
                selectBank: "Select your bank",
                verifyEuropeanId: "Verify with a European ID",
                supportEuropean: "We support the majority of National digital IDs",
                useEuropean: "Use a European ID",
                cantUse: "I can't use any of the above methods",
                help: "If you can't use any of the these methods, please visit our <a href='https://eduid.nl/help' target='_blank' rel='noreferrer'>support pages</a>"
            },
            bank: {
                select: "Select your bank",
                disclaimer: "With iDIN we control through your bank your personal information, to assure who you are. " +
                    "<strong>You will make no payment</strong>. <a href='https://www.idin.nl/' target='_blank' rel='noreferrer'>More about IdIN.</a>",
                anotherMethodPrefix: "If your bank of choice is not in the list, please select ",
                anotherMethodPostfix: "another method"
            }
        },
        feedback: {
            newInstitutionTitle: "was contacted successfully",
            newInstitutionInfo: "The following information has been added to your eduID and can now be shared.",
            validatedGivenName: "Verified given name",
            validatedFamilyName: "Verified family name",
            validatedDayOfBirth: "Verified date of birth"

        },
        issuers: {
            eherkenning: "eIDAS",
            idin: "Idin",
            studielink: "Studielink"
        }
    },
    serviceDesk: {
        confirmIdentityHeader: "You need to manually confirm your identity",
        confirmIdentity: "You must confirm your identity at one of our eduID Service Desks.",
        stepsHeader: "Follow these steps:",
        step1: "Enter your full name as it appears on your ID (first and last name).",
        step2: "Generate a personal verification code.",
        step3: "Present your code and ID to an eduID Service Desk. They will verify your identity manually.",
        redirectWarning: "We will redirect you to another website to enter your name. You won’t be able to log in to <strong>{{service}}</strong> immediately afterward; you will first need to contact a eduID Service Desk.",
        next: "Open website to enter your name"
    }
};
