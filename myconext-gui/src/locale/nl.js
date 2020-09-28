import I18n from "i18n-js";

I18n.translations.nl = {
  header: {
    title: "eduID",
    logout: "Uitloggen"
  },
  landing: {
    logoutTitle: "Je bent uitgelogd",
    logoutStatus: "Om het uitlogproces te voltooien, moet je de browser nu afsluiten.",
    deleteTitle: "Je eduID is verwijderd",
    deleteStatus: "Om het verwijderingsproces te voltooien, moet je je browser nu afsluiten."
  },
  notFound: {
    title: "Oeps...",
    title2: "Er is iets fout gegaan (404)."
  },
  profile: {
    title: "Persoonlijke informatie",
    info: "Basisinformatie zoals je naam en e-mailadres.",
    email: "E-mail",
    schacHomeOrganization: "Instellings ID",
    name: "Naam",
    profile: "Profiel"
  },
  edit: {
    title: "Aanpassen profielgegevens",
    info: "Voer je volledige naam in.",
    givenName: "Je voornaam",
    familyName: "Je achternaam",
    update: "Opslaan",
    cancel: "Annuleren",
    updated: "Je profiel is bijgewerkt.",
    back: "/profile"
  },
  security: {
    title: "Beveiliging",
    subTitle: "We bieden verschillende methoden om in te loggen met je eduID.",
    secondSubTitle: "Loginmethoden",
    usePassword: "Wachtwoord",
    usePublicKey: "WebAuthn",
    notSet: "Niet ingesteld",
    notSupported: "Not supported",
    useMagicLink: "Stuur magische link naar",
    rememberMe: "Ingelogd blijven",
    rememberMetrue: "Ja",
    rememberMefalse: "Nee",
  },
  home: {
    welcome: "Welkom {{name}}",
    profile: "Profiel",
    security: "Beveiliging",
    account: "Account",
    institutions: "Koppelingen",
    services: "Diensten",
    favorites: "Favorieten",
    settings: "Instellingen",
    links: {
      teams: "Teams",
      teamsHref: "https://teams.{{baseDomain}}",
    }
  },
  account: {
    title: "Je eduID account",
    deleteTitle: "Je eduID verwijderen",
    info1: "Je kunt je eduID verwijderen wanneer je maar wilt.",
    info2: "Let op, je verliest het unieke eduID-nummer dat momenteel aan je e-mailadres is gekoppeld. Wanneer je je opnieuw registreert voor eduID met hetzelfde e-mailadres, krijg je een nieuw eduID-nummer. Sommige diensten gebruiken dit unieke nummer om je te identificeren, dus voor die diensten word je dan gezien als een nieuwe gebruiker. ",
    info3: "Houd er rekening mee dat het verwijderen van je eduID niet betekent dat alle diensten die je met je eduID hebt gebruikt, ook je gegevens zullen verwijderen.",
    info4: "Om het verwijderen van je eduID volledig te voltooien, moet je nadat je account is verwijderd je browser afsluiten.",
    deleteAccount: "Verwijder mijn eduID",
    deleteAccountConfirmation: "Weet je zeker dat je je eduID wilt verwijderen?"
  },
  institutions: {
    title: "Gekoppelde instellingen",
    explanation: "Door je onderwijsinstelling aan je eduID te koppelen, kun je via eduID bewijzen dat je aan een onderwijsinstelling studeert of werkt.",
    noInstitutions: "Je hebt geen onderwijsinstelling(en) gekoppeld. Klik op de onderstaande knop als je een instelling wilt koppelen.",
    name: "Korte naam",
    add: "Nieuwe instelling koppelen",
    addInstitutionConfirmation: "Als je doorgaat word je gevraagd in te loggen via de onderwijsinstelling die je wilt koppelen. Selecteer eerst welke instelling je wilt koppelen en log daarna in.<br/> <br/>Nadat je succesvol bent ingelogd kom je hier weer terug.",
    proceed: "Doorgaan",
    addInstitution: "Instelling toevoegen"
  },
  institution: {
    title: "Gekoppelde instelling",
    info: "Deze instelling is op {{date}} om {{hours}}: {{minutes}} gekoppeld aan je eduID.",
    name: "Naam van de instelling",
    eppn: "Identifier bij de instelling",
    displayName: "Gebruikersnaam",
    affiliations: "Betrekking(en) bij de instelling",
    expires: "Koppeling verloopt op",
    expiresValue: "{{date}}",
    delete: "Verwijder Koppeling",
    cancel: "Annuleren",
    deleted: "De koppeling met instelling {{name}} is verwijderd",
    back: "/instellingen",
    deleteInstitution: "Verwijder koppeling",
    deleteInstitutionConfirmation: "Weet je zeker dat je de koppeling met deze instelling wilt verwijderen?<br/> <br/>Sommige diensten vereisen dat je een koppeling hebt met een onderwijsinstelling. Je wordt mogelijk gevraagd een instelling te koppelen als je één van die diensten gebruikt."
  },
  services: {
    title: "Gebruikte diensten",
    info: "Elke service die je met je eduID-account gebruikt, onvangt attributen van je account, waaronder een unieke gepseudonimiseerde eduID om je account te identificeren.",
    explanation: "Dit zijn alle services waar je bent ingelogd met eduID.",
    noServices: "Je hebt nog geen service gebruikt met je eduID-account.",
    name: "Naam",
  },
  service: {
    title: "Dienst",
    info: "Je bent voor de eerste keer ingelogd op deze dienst op {{date}} om {{hours}}: {{minutes}}",
    tokenInfo: "De dienst {{name}} heeft eenAPI token met toegang tot je account.",
    name: "Naam",
    eduId: "EduID",
    access: "Toegang",
    accounts: "Accounts",
    delete: "Verwijderen",
    cancel: "Annuleren",
    revoke: "Intrekken",
    deleted: "Je eduID voor dienst {{name}} is verwijderd",
    back: "/services",
    deleteService: "Verwijder dienst",
    deleteServiceConfirmation: "Weet je zeker dat je je unieke gepseudonimiseerde eduID voor {{name}} wilt verwijderen? <br/> <br/> Deze service herkent je wellicht niet de volgende keer dat je inlogt en je persoonlijke gegevens bij deze service worden mogelijkerwijs verwijderd.",
    deleteTokenConfirmation: "Weet je zeker dat het API access token voor {{name}} wilt intrekken?",
    deleteToken: "Token intrekken",
  },
  credential: {
    title: "Public Key Credential",
    info: "Je hebt deze key toegevoegd op {{date}} om {{hours}}: {{minutes}}",
    name: "Naam",
    delete: "Verwijderen",
    cancel: "Annuleren",
    deleted: "Je key {{name}} is verwijderd",
    back: "/weauthn",
    deleteCredential: "Verwijder key",
    deleteCredentialConfirmation: "Weet je zeker dat je de key {{name}} wilt verwijderen? De key wordt verwijderd uit je eduID-account, maar wordt niet verwijderd uit je browser en / of van je YubiKey-apparaat."
  },
  password: {
    setTitle: "Wachtwoord instellen",
    updateTitle: "Wijzig wachtwoord",
    currentPassword: "Huidig wachtwoord",
    newPassword: "Nieuw wachtwoord",
    confirmPassword: "Bevestig nieuw wachtwoord",
    setUpdate: "Wachtwoord instellen",
    updateUpdate: "Opslaan",
    cancel: "Annuleren",
    set: "Je wachtwoord is ingesteld",
    updated: "Je wachtwoord is aangepast",
    back: "/security",
    passwordDisclaimer: "Kies een wachtwoord van tenminste 8 karakters lang met minimaal een hoofdletter en een cijfer. Een langer wachtwoord van minimaal 15 karakters mag ook.",
    invalidCurrentPassword: "Je huidige wachtwoord is niet correct."
  },
  webauthn: {
    setTitle: "WebAuthn instellen",
    updateTitle: "WebAuthn instellen",
    publicKeys: "Je publieke keys",
    noPublicKeys: "Je hebt nog geen keys toegevoegd.",
    nameRequired: "Voordat je een key kan toevoegen, moet je deze eerst een naam geven.",
    revoke: "Revoke",
    addDevice: "Voeg dit apparaat toe",
    info: "Public Key Cryptography and Web Authentication (ook bekend als WebAuthn) stelt eduID in staat om u te authenticeren met behulp van public key cryptografie in plaats van een magische link of wachtwoord.",
    info2: "Wanneer je op {{action}} klikt, wordt je doorgestuurd naar de eduID-identiteitsserver en vraagt je browser je om je identiteit te bevestigen.",
    back: "/security",
    setUpdate: "Start",
    updateUpdate: "Voeg dit apparaat toe",
    credentialName: "Device naam",
    credentialNamePlaceholder: "e.g. mijn gele YubiKey",
    test: "Test",
    testInfo: "Druk op de <strong>test</strong>knop om een 1 van je WebAuthn-sleutels te testen. Je wordt doorgestuurd naar de eduID-identiteitsserver.",
    testFlash: "Je hebt met succes je WebAuthn-sleutel getest voor authenticatie"
  },
  rememberMe: {
    updated: "Dit apparaat wordt niet langer onthouden",
    forgetMeTitle: "Onthoud dit apparaat",
    info: "Dit apparaat wordt onthouden. Je bent hierdoor automatisch ingelogd met eduID.",
    cancel: "Annuleren",
    update: "Vergeet dit apparaat",
    forgetMeConfirmation: "Weet je zeker dat je dit apparaat niet langer wilt onthouden?",
    forgetMe: "Vergeet dit apparaat"
  },
  footer: {
    privacy: "Privacybeleid",
    terms: "Voorwaarden",
    help: "Help",
    poweredBy: "Aangeboden door"
  },
  modal: {
    cancel: "Annuleren",
    confirm: "Bevestigen"
  },
  migration: {
    header: "Je eduID is aangemaakt!",
    info: "Je Onegini-account is succesvol gemigreerd.",
    info2: "Vanaf nu moet je eduID gebruiken om in te loggen bij diensten waar je voorheen Onegini gebruikte.",
    info3: "Tip! je eduID heeft standaard geen wachtwoord nodig (we sturen een magische link naar je e-mail om in te loggen), maar als je wilt, kan je toch een wachtwoord instellen op het ",
    securityLink: " tabblad Beveiliging.",
    link: "Naar mijn account gegevens"
  },
  migrationError: {
    header: "Let op: eduID met zelfde e-mailadres bestaat al",
    info: "Je hebt al een eduID met hetzelfde e-mailadres als je Onegini-account. Je hebt daarom nu twee keuzes:",
    sub1: "Doorgaan met migreren van je bestaande Onegini-account naar een nieuw eduID. Dit betekent:",
    sub1Inner1: "Teamlidmaatschappen in SURFconext Teams en autorisaties binnen diensten van je bestaande Onegini-account gaan mee naar je nieuwe eduID.",
    sub1Inner2: "Teamlidmaatschappen en autorisaties voor diensten van je bestaande eduID gaan verloren.",
    sub2: "Migratie afbreken en doorgaan met je bestaande eduID. Dit betekent:",
    sub2Inner1: "Je blijft je bestaande eduID gebruiken.",
    sub2Inner2: "Je bestaande Onegini-account kun je per 1 juli 2020 niet meer gebruiken. Teamlidmaatschappen in SURFconext Teams en autorisaties binnen diensten van dit Onegini-account gaan verloren.",
    abortMigration: "Migratie afbreken ",
    continueMigration: "Doorgaan met migreren",
    abort: "afbreken",
    continue: "doorgaan",
    help: "Hulp nodig? Stuur een e-mail naar <a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>."
  },
  format: {
    creationDate: "Je eduID is aangemaakt op {{date}} om {{hours}}:{{minutes}}"
  }
};
