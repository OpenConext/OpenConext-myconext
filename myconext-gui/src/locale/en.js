import I18n from "i18n-js";

I18n.translations.en = {
    header: {
        title: "My SURFconext"
    },
    landing: {
        info: "Collaborate online in higher education",
        login: "Enter",
    },
    notFound: {
        main: "404 - Not Found"
    },
    profile: {
        title: "Personal information",
        info: "Basic information like your name and email address, and the information we have received from your institution",
        email: "Email address",
        name: "Name",
        profile: "Profile"
    },
    edit :{
        title: "Name",
        info: "Please provide your full name",
        givenName: "Your given name",
        familyName: "Your family name",
        update: "Update",
        cancel: "Cancel",
        updated: "Your profile has been updated",
        back: "/profile"
    },
    security: {
        title: "Security",
        subTitle: "We support the following sign in options for My SURFconext:",
        usePassword: "Password",
        notSet: "Not set",
        useMagicLink: "Send magic link to",
    },
    home: {
        welcome: "Welcome {{name}}",
        profile: "Personal info",
        security: "Security",
        account: "Account",
        favorites: "Favorites",
        settings: "Settings",
        links: {
            teams: "Teams",
            teamsHref: "https://teams.{{baseDomain}}",
        }
    },
    account :{
        title: "Your my.SURFconext account",
        info: "You can choose to remove your SURFconext account",
        deleteAccount : "Delete my account",
        deleteAccountConfirmation : "Are you sure you want to delete your my.SURFconext account?"
    },
    password: {
        setTitle: "Set password",
        updateTitle: "Change password",
        currentPassword: "Current password",
        newPassword: "New password",
        confirmPassword: "Confirm new password",
        setUpdate: "Set password",
        updateUpdate: "Update password",
        cancel: "Cancel",
        set: "Your password has been set",
        updated: "Your password has been updated",
        back: "/security",
        passwordDisclaimer: "Make sure it's at least 15 characters OR at least 8 characters including a number and a uppercase letter.",
        invalidCurrentPassword: "Your current password is invalid."
    },
    footer: {
        tip: "Need tip or info?",
        help: "Help & FAQ",
        poweredBy: "Proudly powered by",
        surfconext: "SURFconext",
    },
    modal: {
        cancel: "Cancel",
        confirm: "Confirm"
    }
};

I18n.ts = (key, model) => {
    let res = I18n.t(key, model);
    if (I18n.branding && I18n.branding !== "SURFconext") {
        res = res.replace(/SURFconext/g, I18n.branding);
    }
    return res;
};