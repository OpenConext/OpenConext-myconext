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
        info: "When you use eduID to logon to other websites, some of your personal information needs to be shared. Some services require that your personal information is validated by a third party.",
        basic: "Basic information",
        email: "Email address",
        name: "Name",
        validated: "Validated information",
        firstAndLastName: "First and last name",
        firstAndLastNameInfo: "Your first and lastname are not yet verified by a trusted party",
        verify: "Verify",
        student: "Prove of student",
        studentInfo: "You have not yet proven that you are following education in the Netherlands.",
        prove: "Prove",
        trusted: "Trusted party link",
        trustedInfo: "You eduID account is not yet linked to a trusted party",
        link: "Link",
        institution: "Institution",
        affiliations: "Affiliation(s)",
        expires: "Connection expires",
        expiresValue: "{{date}}",
        verifiedAt: "Verified by <strong>{{name}}</strong> on {{date}}",
        addInstitutionConfirmation: "When you proceed you will be asked to login at the institution you want to connect to your eduID. First, select which institution you want to connect; then, login at that institution.<br/><br/>After a successful login you will come back here.",
        proceed: "Proceed",
        addInstitution: "Add institution"
    },
    eppnAlreadyLinked: {
        header: "Account not linked!",
        info: "Your eduID could not be linked, because the trusted account with which you logged in, is already linked to a different eduID account.",
        proceed: "You can try to link to another institution.",
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
        info: "Please enter your new email. A verification mail will be send to this address.",
        email: "Your new email",
        update: "Request",
        cancel: "Cancel",
        updated: "A mail has been send to {{email}}",
        confirmed: "Your email has been changed to {{email}}",
        back: "/personal",
        emailEquality: "Your new email equals your current email",
        duplicateEmail: "This email is already in use."
    },
    security: {
        title: "Security settings",
        subTitle: "We provide different methods to sign in to your eduID account.",
        secondSubTitle: "Sign-in methods",
        usePassword: "Password",
        usePublicKey: "WebAuthn",
        notSet: "Not set",
        notSupported: "Not supported",
        useMagicLink: "Send magic link to",
        rememberMe: "Stay logged in",
        rememberMetrue: "Yes",
        rememberMefalse: "No",
        securityKey: "Security key {{nbr}}",
        test: "Test",
        addSecurityKey: "Add security key",
        addSecurityKeyInfo: "You can register biometrics (TouchID, FaceID or Windows Hello) or hardware security tokens (Yubikey).",
        settings: "Sign-in settings",
        rememberMeInfo: "<strong>Your device is currently remembered. You will be automatically logged in on eduD</strong>",
        noRememberMeInfo: "When logging in with your eduID, you can choose to <strong>stay logged in</strong>. This remembers your login on the device you use at that moment.",
        forgetMe: "Forget me"
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
        deleteInfo:"Proceed with care, as you will lost the unique eduID identifiers currently associated wth your email address.",
        data: "Download your data",
        personalInfo: "Click the button left to download all your personal data.",
        deleteTitle: "Deleting your eduID account",
        info1: "You can delete your eduID whenever you want.",
        info2: "Proceed with care, as you will lose the unique eduID number currently associated with your email address. When you re-register for a new eduID with that same email address, you will receive a new eduID number. Some services use this unique number to identify you, so for those services you will be treated as a new user.",
        info3: "Please note that deleting your eduID account does not mean all services you accessed with that eduID account will also have your data removed.",
        info4: "To fully complete the process of deleting your eduID account you must close your browser after your account has been removed.",
        deleteAccount: "Delete my account",
        deleteAccountConfirmation: "Are you sure you want to delete your eduID account?",
        deleteAccountSure: "Delete your account for all eternity?",
        deleteAccountWarning: "There is no way to revert this action.",
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
        deleteServiceConfirmation: "Are you sure you want to delete your unique pseudonymized eduID for {{name}} and revoke access to your linked accounts?<br/><br/>This service might not recognize you the next time you login and all your personal data with this Service could be deleted.",
        deleteTokenConfirmation: "Are you sure you want to revoke the API access token for {{name}}?",
        deleteToken: "Revoke token",
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
        info: "You added this key on {{date}} at {{hours}}:{{minutes}}",
        name: "Security key name",
        cancel: "Cancel",
        update: "Update",
        deleted: "Your key {{name}} has been deleted",
        updated:"Your key {{name}} has been updated",
        back: "/weauthn",
        deleteCredential: "Delete key",
        deleteCredentialConfirmation: "Are you sure you want to delete your public key credential {{name}}? The key will be deleted from your eduID account, but will not be removed from your browser and / or YubiKey device."
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
        flash: {
            passwordLink: "An email has been sent to {{name}} with a link to reset your password"
        }
    },
    webauthn: {
        setTitle: "Enable WebAuthn",
        updateTitle: "Add security key",
        publicKeys: "Your public keys",
        noPublicKeys: "You have not added any keys. ",
        nameRequired: "Before you can add a new key you will need to give it a name.",
        revoke: "Revoke",
        addDevice: "Add device",
        info: "You can choose to use a Bluetooth security key, USB security key or the security key built into your device.",
        back: "/security",
        setUpdate: "Start",
        updateUpdate: "Add device",
        credentialName: "Security key name",
        credentialNamePlaceholder: "e.g. my red YubiKey",
        test: "Test",
        testInfo: "Hit the <strong>test</strong> button to test one of your WebAuthn keys. You will be redirected to the eduID identity server.",
        testFlash: "You successfully tested your WebAuthn key to authenticate"
    },
    rememberMe: {
        updated: "Your device is no longer remembered",
        forgetMeTitle: "Remember this device.",
        info: "Your device is currently remembered. You will be automatically logged in on the eduID.",
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
    }
};
