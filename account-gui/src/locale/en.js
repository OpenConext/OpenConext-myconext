import I18n from "i18n-js";

I18n.translations.en = {
    login: {
        header: "Use SURFconext",
        header2: "to login to {{name}}",
        trust: "Trust this computer",
        email: "Your email address",
        emailPlaceholder: "Email address",
        familyName: "Family name",
        givenName: "Given name",
        familyNamePlaceholder: "e.g. Berners-Lee",
        givenNamePlaceholder: "e.g. Tim",
        usePassword: "Use password",
        useMagicLink: "Use magic link",
        sendMagicLink: "Send magic link",
        rememberMe: "Remember this device",
        password: "Your password",
        passwordForgotten: "If you forgot your password,",
        passwordForgottenLink: " use the magic link",
        login: "Login",
        create: "Create",
        newTo: "New to surfConext?",
        createAccount: " Create an account.",
        useExistingAccount: "Use existing account",
        invalidEmail: "Invalid email",
        requiredAttribute: "{{attr}} is required",
        emailInUse: "Email is already in use.",
        emailNotFound: "Email not found.",
        emailOrPasswordIncorrect: "Email or password are incorrect",
        passwordDisclaimer: "Make sure it's at least 15 characters OR at least 8 characters including a number and a uppercase letter.",
        noGuestAccount: "<strong>Don't</strong> have a Guest Account yet?",
        noGuestAccountInfo: "A SURFconext Guest account is meant for users who wants to use services via <a href=\"https://surfconext.nl\" target=\"_blank\">SURFconext</a>, but who don't have a institutional account.",
        requestGuestAccount: "Request a Guest Account",
        alreadyGuestAccount: "<strong>Already</strong> have a Guest Account?",
        noPasswordNeeded: "No password needed. We'll send a magic link to your email to sign in instantly.",
        usePasswordLink: "Type a password anyway."
    },
    magicLink: {
        header: "Check",
        header2: "your inbox",
        info: "We have sent an email with a magic link to:",
        wrongEmail: "Woops, wrong email"
    },
    confirm: {
        header: "Success",
        header2: "your account is activated",
        thanks: "Thank you for verifying your email address.",
        info: "Click on the link below to proceed to <strong>{{name}}</strong> with your new Guest Account.",
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
    },
    expired: {
        title: "Your Magic Link is expired.",
        info: "The Magic Link you have used is either expired or already used.<br/><br/>  Please go back to the service you where heading to and request a new Magic Link."
    },
    notFound: {
        title: "404",
        title2: "Not Found"
    }
};

I18n.ts = (key, model) => {
    let res = I18n.t(key, model);
    if (I18n.branding && I18n.branding !== "SURFconext") {
        res = res.replace(/SURFconext/g, I18n.branding);
    }
    return res;
};