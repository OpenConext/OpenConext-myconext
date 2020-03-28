import I18n from "i18n-js";

I18n.translations.en = {
    login: {
        header: "Use your eduID to login to",
        header2: "Request",
        header3: "your eduID",
        trust: "Trust this computer",
        email: "Your personal email address",
        emailPlaceholder: "e.g. user@gmail.com",
        familyName: "Last name",
        givenName: "First name",
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
        emailNotFound: "Email not found.",
        emailOrPasswordIncorrect: "Email or password are incorrect",
        passwordDisclaimer: "Make sure it's at least 15 characters long OR at least 8 characters when including a number and an UpperCase letter.",
        noGuestAccount: "<strong>Don't</strong> have an eduID yet?",
        requestGuestAccount: "Request an eduID",
        alreadyGuestAccount: "<strong>Already</strong> have an eduID?",
        noPasswordNeeded: "No password needed. We'll send a magic link to your email address to sign in instantly.",
        usePasswordLink: "Type a password anyway",
        usePasswordLinkInfo: " if you have set one.",
        whatis: "What is eduID?",
        agreeWithTerms: "<span>I agree with <a href='https://eduid.nl/terms_of_service/' target='_blank'>the terms of service.</a> I also understand <a href='https://eduid.nl/privacy_policy/' target='_blank'>the privacy policy</a>.</span>"
    },
    magicLink: {
        header: "Please",
        header2: "Check your email",
        info1: "We've sent you an email at",
        info2: "It contains a magic link that will sign you in.",
        wrongEmail: "Is the above email address incorrect?",
        wrongEmail2: "Please start over."
    },
    confirm: {
        header: "Success!",
        thanks: "Your eduID account has been created. Proceed to your destination."
    },
    footer: {
        privacy: "Privacy policy",
        terms: "Terms of Use",
        help: "Help",
        poweredBy: "Powered by"
    },
    session: {
        title: "Your session was lost.",
        info: "You must open the magic link from the email in the same browser session as where you requested the magic link. <br/><br/>  Please go back to the service you were heading to and request a new magic link."
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
    migration: {
        header: "Migrate to an <br/>eduID guest account",
        info1: "SURF will phase out the use of Onegini. To retain access, you must migrate your Onegini account to an eduID account.",
        info2: "You only need to click the button and log in with your existing Onegini account once. We will then migrate your account to eduID and send you an email after completion.",
        link: "Start migration"
    }
};

I18n.ts = (key, model) => I18n.t(key, model);
