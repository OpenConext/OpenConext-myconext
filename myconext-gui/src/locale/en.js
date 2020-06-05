import I18n from "i18n-js";

I18n.translations.en = {
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
    info: "Basic information like your name and email address.",
    email: "Email address",
    schacHomeOrganization: "Institution ID",
    name: "Name",
    profile: "Profile"
  },
  edit: {
    title: "Name",
    info: "Please provide your full name",
    givenName: "Your first name",
    familyName: "Your last name",
    update: "Update",
    cancel: "Cancel",
    updated: "Your profile has been updated",
    back: "/profile"
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
  },
  home: {
    welcome: "Welcome {{name}}",
    profile: "Personal info",
    security: "Security",
    account: "Account",
    institutions: "Institutions",
    favorites: "Favorites",
    settings: "Settings",
    links: {
      teams: "Teams",
      teamsHref: "https://teams.{{baseDomain}}",
    }
  },
  account: {
    title: "Your eduID account",
    deleteTitle: "Deleting your eduID account",
    info1: "You can delete your eduID whenever you want.",
    info2: "Proceed with care, as you will lose the unique eduID number currently associated with your email address. When you re-register for a new eduID with that same email address, you will receive a new eduID number. Some services use this unique number to identify you, so for those services you will be treated as a new user.",
    info3: "Please note that deleting your eduID account does not mean all services you accessed with that eduID account will also have your data removed.",
    info4: "To fully complete the process of deleting your eduID account you must close your browser after your account has been removed.",
    deleteAccount: "Delete my account",
    deleteAccountConfirmation: "Are you sure you want to delete your eduID account?"
  },
  institutions: {
    title: "Linked institutions",
    info: "You can link your eduID account to one or more educational institutions.",
    explanation: "Linking your primary educational institution to you eduID account will enable you in the future to register for classes at several institutions simultaneously.",
    noInstitutions: "You have not linked any educational institution. Click on the button below to add an institution.",
    name: "Short name",
    add: "Add institution",
    addInstitutionConfirmation: "When you proceed you will be redirect to the SURFconext WAYF where can you select the institution that you wan to.<br/><br/>After confirming your identity at that institution you will return here.",
    proceed: "Proceed",
    addInstitution: "Add institution"
  },
  institution: {
    title: "Institution",
    info: "This institution was linked to your eduID account on {{date}} at {{hours}}:{{minutes}}",
    name: "Short name",
    eppn: "Your EPPN",
    expires: "Expires",
    expiresValue: "{{date}}",
    delete: "Delete",
    cancel: "Cancel",
    deleted: "Your institution link with {{name}} has been deleted",
    back: "/institutions",
    deleteInstitution: "Delete institution",
    deleteInstitutionConfirmation: "Are you sure you want to delete this linked institution from your eduID account?<br/><br/>Some services require linked institutions and you might be prompted to link an institution if you access one of those services."
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
  webauthn: {
    setTitle: "Enable WebAuthn",
    updateTitle: "Configure WebAuthn",
    publicKeys: "Your public keys",
    revoke: "Revoke",
    addDevice: "Add device",
    info: "Public Key Cryptography and Web Authentication (a.k.a. WebAuthn) allows eduID to authenticate you using public key cryptography instead of a magic link or password.",
    info2: "When your click on {{action}} you will be redirected to the eduID identity server and your browser will prompt you for your identity.",
    back: "/security",
    setUpdate: "Start",
    updateUpdate: "Add device",
    currentKeys: "You currently have registered {{count}} public key(s)."

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
    creationDate: "Your eduID account was created on {{date}} at {{hours}}:{{minutes}}"
  }
};

I18n.ts = (key, model) => I18n.t(key, model);
