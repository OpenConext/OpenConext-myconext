import I18n from "i18n-js";

I18n.translations.en = {
    sidebar: {
        home: "Home",
        personalInfo: "Personal info",
        dataActivity: "Data & activity",
        security: "Security",
        account: "Account"
    },
    start: {
        hi: "Hi {{name}}!",
        manage: "Manage your personal info, your privacy, and the security of your eduID account."
    },
    header: {
        title: "eduID",
        logout: "Logout"
    },
    landing: {
        logoutTitle: "You have been logged out",
        logoutStatus: "To finalise the logout process you must now close this browser.",
        deleteTitle: "Your eduID account has been deleted",
        deleteStatus: "To finalise the removal process you must now close this browser."
    },
    notFound: {
        title: "Whoops...",
        title2: "Something went wrong (404)."
    },
    profile: {
        title: "Personal information",
        info: "When you use eduID to login to other websites, some of your personal information will be shared. Some websites require that your personal information is validated by a third party.",
        basic: "Basic information",
        email: "Email address",
        name: "Name",
        validated: "Validated information",
        firstAndLastName: "First and last name",
        firstAndLastNameInfo: "Your first and lastname are not yet verified by a third party",
        verify: "Verify",
        student: "Proof of student",
        studentInfo: "You have not yet proven that you are a student in the Netherlands.",
        prove: "Prove",
        trusted: "Link with third party",
        trustedInfo: "You eduID account is not yet linked to a third party",
        link: "Link",
        institution: "Institution",
        affiliations: "Affiliation(s)",
        expires: "Link expires",
        expiresValue: "{{date}}",
        verifiedAt: "Verified by <strong>{{name}}</strong> on {{date}}",
        proceed: "Proceed",
        verifyFirstAndLastName: {
            addInstitution: "Verify name",
            addInstitutionConfirmation: "When you proceed you will be asked to login at the institution you want to link to your eduID. First, select which institution you want to connect; then, login at that institution.<br/><br/>After a successful login you will come back here.",
        },
        verifyStudent: {
            addInstitution: "Prove student",
            addInstitutionConfirmation: "When you proceed you will be asked to login at the institution you want to link to your eduID. First, select which institution you want to connect; then, login at that institution.<br/><br/>After a successful login you will come back here.",
        },
        verifyParty: {
            addInstitution: "Link party",
            addInstitutionConfirmation: "When you proceed you will be asked to login at the institution you want to link to your eduID. First, select which institution you want to connect; then, login at that institution.<br/><br/>After a successful login you will come back here.",
        }
    },
    eppnAlreadyLinked: {
        header: "Connection not added!",
        info: "Your eduID could not be linked. The trusted account with which you just logged in, is already linked to a different eduID account: {{email}}.",
        retryLink: "Retry"
    },
    edit: {
        title: "Name",
        info: "Please provide your full name",
        givenName: "Your first name",
        familyName: "Your last name",
        update: "Update",
        cancel: "Cancel",
        updated: "Your profile has been updated",
        back: "/personal"
    },
    email: {
        title: "Email",
        info: "Please enter your new email address. A verification mail will be sent to this address.",
        email: "Your new email",
        update: "Request",
        cancel: "Cancel",
        updated: "A verification email has been sent to {{email}}",
        confirmed: "Your email address has been changed to {{email}}",
        back: "/personal",
        emailEquality: "Your new email address is the same as your current email",
        duplicateEmail: "This email address is already in use.",
        outstandingPasswordForgotten: "Outstanding password forgotten request",
        outstandingPasswordForgottenConfirmation: "You have requested a password forgotten link. This link will no longer be valid if you confirm your email change."
    },
    security: {
        title: "Security settings",
        subTitle: "We provide different methods to sign in to your eduID account.",
        secondSubTitle: "Other sign-in methods",
        usePassword: "Password",
        usePublicKey: "WebAuthn",
        notSet: "Not set",
        notSupported: "Not supported",
        useMagicLink: "Send magic link to",
        rememberMe: "Stay logged in",
        securityKey: "Security key {{nbr}}",
        test: "Test",
        addSecurityKey: "Add security key",
        addSecurityKeyInfo: "You can add security keys to your eduID account which can be used to login. You can use, for example, the built-in sensor of your device (TouchID, FaceID) or a separate hardware key (YubiKey).",
        settings: "Sign-in settings",
        rememberMeInfo: "<strong>Your device is currently remembered. You will be automatically logged in to eduID.</strong>",
        noRememberMeInfo: "When logging in with your eduID, you can choose to <strong>stay logged in</strong>. This remembers your login on the device you use at that moment.",
        forgetMe: "Forget me",
        tiqr: {
            title: "Want to sign in quicker and more secure next time?",
            info: "Get the <strong>eduID app</strong> and securely sign in without passwords or accessing your email.",
            fetch: "Get it now",
            deactivate: "Deactivate",
            app: "eduID app",
            phoneId: "Phone ID",
            APNS: "iPhone",
            GCM: "Android",
            appCode: "Appcode",
            lastLogin: "Last login",
            activated: "Activated on",
            dateTimeOn: "on",
        }
    },
    home: {
        home: "Home",
        welcome: "Welcome {{name}}",
        "data-activity": "Data & activity",
        personal: "Personal info",
        security: "Security",
        account: "Account",
        institutions: "Connections",
        services: "Services",
        favorites: "Favorites",
        settings: "Settings",
        links: {
            teams: "Teams",
            teamsHref: "https://teams.{{baseDomain}}"
        }
    },
    account: {
        title: "Your account",
        titleDelete: "Delete your eduID account",
        info: "On this page you can manage your account.",
        created: "Created on",
        delete: "Delete my account",
        cancel: "Cancel",
        deleteInfo: "Proceed with care, as you will lose the unique eduID identifiers currently associated wth your email address.",
        data: "Download your data",
        personalInfo: "Click the button left to download all your personal data from your eduID account.",
        downloadData: "Download",
        downloadDataConfirmation: "The download of your personal data from your eduID account contains all the information we have about you. It also contains technical keys and references.",
        deleteTitle: "Deleting your eduID account",
        info1: "You can delete your eduID whenever you want.",
        info2: "Proceed with care, as you will lose the unique eduID number currently associated with your email address. When you re-register for a new eduID with that same email address, you will receive a new eduID number. Some services use this unique number to identify you, so for those services you will be treated as a new user.",
        info3: "Please note that deleting your eduID account does not mean all services you accessed with that eduID account will also have your data removed.",
        info4: "To fully complete the process of deleting your eduID account you must close your browser after your account has been removed.",
        deleteAccount: "Delete my account",
        deleteAccountConfirmation: "Are you sure you want to delete your eduID account?",
        deleteAccountSure: "Delete your account for all eternity?",
        deleteAccountWarning: "There is no way to undo this action.",
        proceed: "If you wish to proceed, please type in your full name for confirmation.",
        name: "Full name",
        namePlaceholder: "Your full name as used on your profile"
    },
    dataActivity: {
        title: "Data & Activity",
        info: "Each service you accessed through eduID receives certain personal data (attributes) from your eduID account. For example, your name, your email address or a pseudonym which the service can use to uniquely identify you.",
        explanation: "Apps you logged in to using eduID.",
        noServices: "You did not yet use eduID to login to any service.",
        name: "Name",
        add: "Connect new institution",
        access: "Can access your data",
        details: {
            login: "Login details",
            delete: "Delete login details",
            first: "First login",
            eduID: "Unique eduID",
            homePage: "Homepage",
            deleteDisclaimer: "Deleting these login details means eduID removes this information from your eduID account. You still have an account at the service itself. If you want that removed, please do so at the service.",
            access: "Access rights",
            details: "Access details",
            consent: "Date of consent",
            expires: "Date of expiry",
            revoke: "Revoke access"
        },
        deleteService: "Delete service",
        deleteServiceConfirmation: "Are you sure you want to delete your unique pseudonymised eduID for {{name}} and revoke access to your linked accounts?<br/><br/>This service might not recognize you the next time you login and all your personal data within this service might be lost.",
        deleteTokenConfirmation: "Are you sure you want to revoke the API access token for {{name}}?",
        deleteToken: "Revoke access token",
        deleted: "eduID removed",
        tokenDeleted: "Tokens removed"

    },
    institution: {
        title: "Connected institution",
        info: "This institution was connected to your eduID account on {{date}} at {{hours}}:{{minutes}}",
        name: "Institution name",
        eppn: "Identifier at institution",
        displayName: "Display name",
        affiliations: "Affiliation(s) at institution",
        expires: "This connection expires at",
        expiresValue: "{{date}}",
        delete: "Remove connection",
        cancel: "Cancel",
        deleted: "The connection with your institution {{name}} has been removed",
        back: "/institutions",
        deleteInstitution: "Delete institution",
        deleteInstitutionConfirmation: "Are you sure you want to delete the connection with this institution?<br/><br/>Some services require that you your eduID is connected to an institution. You might be prompted to connect an institution if you access one of those services."
    },
    credential: {
        title: "Edit security key",
        info: "You added this security key on {{date}} at {{hours}}:{{minutes}}",
        name: "Security key name",
        cancel: "Cancel",
        update: "Update",
        deleted: "Your security key {{name}} has been deleted",
        updated: "Your security key {{name}} has been updated",
        back: "/weauthn",
        deleteCredential: "Delete security key",
        deleteCredentialConfirmation: "Are you sure you want to delete your security key {{name}}? The secuirity key will be deleted from your eduID account, but will not be removed from your browser and / or Yubikey device."
    },
    password: {
        setTitle: "Set password",
        updateTitle: "Change password",
        resetTitle: "Reset forgotten password",
        currentPassword: "Current password",
        newPassword: "New password",
        confirmPassword: "Confirm new password",
        setUpdate: "Set password",
        updateUpdate: "Update password",
        cancel: "Cancel",
        set: "Your password has been set",
        reset: "Your password has been reset to a new password",
        updated: "Your password has been updated",
        back: "/security",
        passwordDisclaimer: "Make sure it's at least 15 characters OR at least 8 characters including a number and a uppercase letter.",
        invalidCurrentPassword: "Your current password is invalid.",
        passwordResetHashExpired: "Your password reset link has expired. ",
        forgotPassword: "Help! I forgot my current password",
        passwordResetSendAgain: "Send an email to reset my password.",
        forgotPasswordConfirmation: "Forgot your password? Press 'Confirm' below to instantly receive an email with a link to reset your current password.",
        outstandingEmailReset: "Outstanding email change request",
        outstandingEmailResetConfirmation: "You have an outstanding new email confirmation link. This link will no longer be valid if you confirm your password forgotten request.",
        flash: {
            passwordLink: "An email has been sent to {{name}} with a link to reset your password"
        }
    },
    webauthn: {
        setTitle: "Add security key",
        updateTitle: "Add security key",
        publicKeys: "Your public tokens",
        noPublicKeys: "You have not added any security keys. ",
        nameRequired: "Before you can add a new security key you must give it a name.",
        revoke: "Revoke",
        addDevice: "Add device",
        info: "You can choose to use a Bluetooth security key, USB security key or the security key built into your device.",
        back: "/security",
        setUpdate: "Start",
        updateUpdate: "Add device",
        credentialName: "Security key",
        credentialNamePlaceholder: "e.g. my red Yubikey",
        test: "Test",
        testInfo: "Click the <strong>test</strong> button to test one of your security keys. You will be redirected to the eduID login screen.",
        testFlash: "You successfully tested your security key to authenticate"
    },
    rememberMe: {
        updated: "Your device is no longer remembered",
        forgetMeTitle: "Remember this device.",
        info: "Your device is currently remembered. You will be automatically logged in to eduID.",
        cancel: "Cancel",
        update: "Forget me",
        forgetMeConfirmation: "Are you sure you no longer want this device remembered?",
        forgetMe: "Forget this device"
    },
    footer: {
        privacy: "Privacy policy",
        terms: "Terms of Use",
        help: "Help",
        poweredBy: "Powered by"
    },
    modal: {
        cancel: "Cancel",
        confirm: "Confirm"
    },
    migration: {
        header: "Your eduID has been created!",
        info: "Your Onegini account is succesfully migrated.",
        info2: "From now on you must use your eduID guest account to logon to services where you previously used Onegini.",
        info3: "Tip! Your eduID account by default doesn’t need a password (we will send a magic link to your email to sign you in), but if you want to, you can set one right now under the",
        securityLink: " Security tab.",
        link: "Show my account details"
    },
    migrationError: {
        header: "Warning: email address already used for eduID",
        info: "You already have an {{email}} eduID with the same email address as your Onegini account. You must therefore choose how you wish to proceed:",
        sub1: "Continue with migrating your existing Onegini account to a new eduID. This means:",
        sub1Inner1: "Existing SURFconext Teams memberships and authorisations within services belonging to your current Onegini account will be transferred to your new eduID.",
        sub1Inner2: "Team memberships and authorisations linked to your current eduID will be lost.",
        sub2: "Abort migration and continue with your existing eduID. This means:",
        sub2Inner1: "You will continue to use your current eduID.",
        sub2Inner2: "You can no longer use your existing Onegini account per 1 July 2020. Existing SURFconext Teams memberships and authorisations within services belonging to your current Onegini account will be lost.",
        abortMigration: "Abort migration",
        continueMigration: "Continue migration",
        abort: "abort",
        continue: "continue",
        help: "Need help? Send an email to <a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>."
    },
    format: {
        creationDate: "{{date}} at {{hours}}:{{minutes}}"
    },
    getApp: {
        header: "Download the eduID app",
        info: "Download and install <a href=\"https://eduid.nl/help\" target=\"_blank\">the eduID app</a> (issued by SURF) on your mobile device.",
        google: "https://play.google.com/store/apps/details?id=nl.eduvpn.app&hl=en&gl=US",
        apple: "https://apps.apple.com/us/app/eduvpn-client/id1292557340",
        after: "When you've downloaded the eduID app on your phone, come back here and click next.",
        back: "Back",
        next: "Next"
    },
    sms: {
        header: "Check your phone",
        info: "Enter the six-digit code we sent to your phone to continue",
        codeIncorrect: "The code is incorrect"
    },
    enrollApp: {
        header: "Finish setup in the eduID app",
        scan: "Scan this QR code with your eduID app",
    },
    recovery: {
        header: "Set up an APP recovery method",
        info: "If you can't access eduID with the app or via email, you can use a recovery method to sign in to your eduID account.",
        methods: "The following methods are available.",
        phoneNumber: "Add a recovery phone number.",
        phoneNumberInfo: "You'll receive a text message with a code.",
        backupCode: "Request a backup code.",
        backupCodeInfo: "The code can be used to sign in with.",
        save: "Save the code somewhere safe.",
        active: "This code is active now, but you can generate a new code within My-eduID anytime.",
        copy: "Copy the code",
        copied: "Copied",
        continue: "My code is safe. Continue",
    },
    phoneVerification: {
        header: "Add a recovery phone number",
        info: "Your phone number will be used for security purposes, such as helping you get back into your account if you ever lose your app",
        text: "We will text you a code to verify your number",
        verify: "Verify this phone number",
        placeHolder: "0612345678",
        phoneIncorrect: "Phone number is incorrect"
    },
    congrats: {
        header: "Success",
        info: "You can now use the eduID app to quickly login to services which require you to login with your eduID.",
        next: "Finish"
    },
    deactivate: {
        titleDelete: "Deactivate your eduID app",
        info: "You can deactivate your eduID app if you want to reinstall this app or if you have a new device.",
        recoveryCode: "Backup recovery code",
        recoveryCodeInfo: "Enter the <strong>backup recovery code</strong> from the eduID app enrollment.",
        verificationCode: "SMS verification code",
        codeIncorrect: "Wrong backup recovery code",
        next: "Next",
        deactivateApp: "Deactivate",
        sendSms: "Press next to send a verification code SMS to your registered phone number"
    }
};
