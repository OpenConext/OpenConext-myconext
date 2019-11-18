import I18n from "i18n-js";

I18n.translations.en = {
    login: {
        header: "<strong>Login</strong> with your Guest Account",
        trust: "Trust this computer",
        email: "Your email address",
        familyName: "Family name",
        givenName: "Given name",
        familyNamePlaceholder: "e.g. Berner-Lee",
        givenNamePlaceholder: "e.g. Tom",
        usePassword: "Use password",
        useMagicLink: "Use magic link",
        sendMagicLink: "Send magic link",
        rememberMe: "Remember this device",
        password: "Your password",
        passwordForgotten: "If you forgot your password, use the magic link",
        magicLinkText: "No password needed. We'll send a magic link to your email to sign in instantly.",
        login: "Login",
        create: "Create",
        newTo: "New to surfConext?",
        createAccount: " Create an account.",
        useExistingAccount: "Use existing account",
        invalidEmail: "Invalid email",
        requiredAttribute: "{{attr}} is required",
        emailInUse: "Email is already in use.",
        emailNotFound: "Email not found.",
        passwordDisclaimer: "Make sure it's at least 15 characters OR at least 8 characters including a number and a uppercase letter.",
        noGuestAccount: "<strong>Don't</strong> have a Guest Account yet?",
        noGuestAccountInfo: "A SURFconext Guest account is meant for users who wants to use services via <a href=\"https://surfconext.nl\" target=\"_blank\">SURFconext</a>, but who don't have a institutional account.",
        requestGuestAccount: "Request a Guest Account",
        alreadyGuestAccount: "<strong>Already</strong> have a Guest Account?",

    },
    magicLink: {
        header: "An email is send to {{email}} with a magic link to proceed to your destination."
    },
    confirm: {
        header: "An account was created for {{email}}.",
        link: "Proceed to your destination"
    },
    footer: {
        tip: "Need tip or info?",
        help: "Help & FAQ",
        poweredBy: "Proudly powered by",
        surfconext: "SURFconext",

    },
    session: {
        title: "Your Session was lost.",
        info: "You must open the Magic Link from the mail in the same browser session as where you requested the Magic Link. <br/><br/>  Please go back to the service you where heading to and request a new Magic Link."
    }
};