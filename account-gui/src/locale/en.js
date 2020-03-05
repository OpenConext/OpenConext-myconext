import I18n from "i18n-js";

I18n.translations.en = {
    login: {
        header: "Use your eduID to login to",
        header2: "Request",
        header3: "Your eduID",
        trust: "Trust this computer",
        email: "Email address",
        emailPlaceholder: "e.g. user@example.com",
        familyName: "Family name",
        givenName: "Given name",
        familyNamePlaceholder: "e.g. Berners-Lee",
        givenNamePlaceholder: "e.g. Tim",
        usePassword: "Use password",
        useMagicLink: "Use magic link",
        sendMagicLink: "Send magic link",
        requestEduId: "Request your eduID",
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
        emailInUse: "Email is already in use.",
        emailNotFound: "Email not found. To request a new account,",
        emailNotFoundLink: " click here.",
        emailOrPasswordIncorrect: "Email or password are incorrect",
        passwordDisclaimer: "Make sure it's at least 15 characters OR at least 8 characters including a number and a uppercase letter.",
        noGuestAccount: "<strong>Don't</strong> have an eduID yet?",
        requestGuestAccount: "Request an eduID",
        alreadyGuestAccount: "<strong>Already</strong> have a eduID?",
        noPasswordNeeded: "No password needed. We'll send a magic link to your email to sign in instantly.",
        usePasswordLink: "Type a password anyway ",
        usePasswordLinkInfo: "if you have set one.",
        whatis: "What is eduID?",
        agreeWithTerms: "<span>I agree with <a href='https://www.eduid.nl' target='_blank'>the terms of service.</a></span>"
    },
    magicLink: {
        header: "Please",
        header2: "Check your email",
        info1: "We sent an email to you at",
        info2: "It has a magic link that will sign you in.",
        wrongEmail: "Is the above email not correct?",
        wrongEmail2: "Please start over."
    },
    confirm: {
        header: "Success",
        header2: "your account is activated",
        thanks: "Thank you for verifying your email address.",
        info1: "Click on the link below to proceed to",
        info2: "with your new Guest Account.",
        link: "Proceed to your destination"
    },
    footer: {
        privacy: "Privacy policy",
        terms: "Terms of Use",
        help: "Help",
        poweredBy: "Powered by"
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
    },
    migration: {
        header: "Migrate to a <br/>eduID guest account",
        info1: "SURF will phase out the use of Onegini. To retain access, you must migrate your Onegini account to an eduID guest account.",
        info2: "You only need to click the button and log in with your existing Onegini account once. We will then migrate your account to a new SURConext Guest Account and send you an email after completion.",
        link: "Start migration"
    }
};

I18n.ts = (key, model) => {
    let res = I18n.t(key, model);
    if (I18n.branding && I18n.branding !== "SURFconext") {
        res = res.replace(/SURFconext/g, I18n.branding);
    }
    return res;
};