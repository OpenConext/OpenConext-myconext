# MyConext
[![JAVA CI](https://github.com/OpenConext/OpenConext-myconext/actions/workflows/actions.yml/badge.svg)](https://github.com/OpenConext/OpenConext-myconext/actions/workflows/actions.yml)
![Coverage](.github/badges/jacoco.svg)
![Branches](.github/badges/branches.svg)

An IdP for OpenConext. A user can create and manage his own identity. Authentication uses a magic-link by default, and FIDO2 or a password can be added later.

## Content

- [Getting started](#getting-started)
	- [System Requirements](#system-requirements)
- [Building and running](#building-and-running)
	- [Database and Maipit](#database-and-maipit)
	- [MyConext-Server](#myconext-server)
	- [Account-GUI](#account-gui-idp)
	- [MyConext-GUI](#myconext-gui-sp)
	- [Servicedesk-GUI](#servicedesk-gui-sp)
	- [Public-GUI](#public-gui-content-website)
	- [Build](#build)
	- [Mail](#mail)
    - [Cron](#cron)
	- [Crypto](#crypto)
	- [Translations](#translations)
	- [Miscellaneous](#miscellaneous)
	- [Migration](#migration)
	- [Attribute Manipulation](#attribute-manipulation)
	- [Attribute Aggregation](#attribute-aggregation)
	- [OpenAPI Documentation](#openapi-documentation)
	- [IDIN & e-Herkenning](#idin--e-herkenning)
	- [Running the IdP and testing localhost](#running-the-idp-and-testing-localhost)
- [How to use](#how-to-use)
	- [IDP Flow](#idp-flow)

## Getting started

### System Requirements

- Java 21
- Maven 3
- MongoDB 7.x
- Yarn 1.x
- NodeJS (version 24.3.0)
- Mailpit

## Building and running

### Database and Maipit

The `docker-compose.yaml` file in this project is meant for local development and contains a Mongo database and Mailpit instance

```shell
docker compose up -d
```

### MyConext-Server

Spring Boot backend implementing the eduID/SURFconext "MyConext" Identity Provider: SAML2 and
OIDC authentication flows, MongoDB-backed user/session persistence, RSA-based SAML request
signing, attribute manipulation/aggregation APIs, and the OpenAPI/Swagger documentation for all
of it.

To run locally, type:

```shell
cd myconext-server
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

When developing, it's convenient to just execute the applications main-method, which is in [Application](myconext-server/src/main/java/myconext/MyConextServerApplication.java).
Don't forget to set the active profile to dev.

### Account-GUI (IDP)

The Account-GUI is the SAML/OIDC Identity Provider frontend for MyConext. It is the screen a user
lands on when a Service Provider redirects them to the "Local SURFconext Guest IdP" / "Local eduID
IdP" — it drives the magic-link, password, FIDO2/WebAuthn and Tiqr (mobile app) sign-in flows, as
well as account-linking and step-up/MFA screens.

There is **no home page**: the app only makes sense as the target of an authentication redirect
coming from a Service Provider (e.g. the OIDC-Playground, MyConext-GUI or Servicedesk-GUI). Visiting
it directly without a valid request id will land you on the "Whoops... Something went wrong (404)"
route, which is expected.

The IdP is also built with Svelte and to get initially started:

```shell
cd account-gui
nvm use
yarn install
yarn dev
```
There is no home page, you'll need to visit an SP and choose "Local SURFconext Guest IdP" to login. App is running on port 3000.

### MyConext-GUI (SP)

MyConext-GUI is the "My eduID" self-service Service Provider frontend for MyConext. Once a user is
authenticated (via [`account-gui`](../account-gui)), this is where they land to manage their own
eduID identity: personal info, linked (institution/external) accounts, security methods (password,
FIDO2/WebAuthn, the Tiqr mobile app), connected services and account deletion.

Like `account-gui`, this app has no meaningful anonymous landing page for most routes: on mount it
calls `/myconext/api/sp/me`, and if the user isn't authenticated it redirects to the configured
`loginUrl` (see [`src/App.svelte`](src/App.svelte)). A small set of routes (`/create-from-institution`,
`/landing`, `/install-app`) are reachable without an existing session, to support the "create an
eduID linked to your institution account" flow for guests.

The myconext ServiceProvider is built with Svelte and to get initially started:

```shell
cd myconext-gui
nvm use
yarn install
yarn dev
```

Browse to the [application homepage](http://localhost:3001/).

### Servicedesk-GUI (SP)

ServiceDesk-GUI is the internal tool SURF/SURFconext service-desk staff use to perform **in-person
identity verification** for eduID users. A student (or other eduID user) who needs a formally
verified identity generates a numeric verification code in the eduID app, visits (or calls) the
service desk, and a service-desk employee uses this application to: look up the code, manually
check the person's ID document against the data on file, and approve the check — after which the
person's identity is marked as verified in eduID.

There is no self-service function here: every route except `/login` requires an authenticated,
authorized service-desk employee (see [Overview](#overview)).

The myconext servicedesk is built with React and Vite and to get initially started:

```shell
cd servicedesk-gui
yarn install
yarn dev
```

Browse to the [application homepage](http://localhost:3003/).

### Public-GUI (Content website)

Public-GUI is the public-facing marketing/content site for **eduID** — the informational website a
visitor lands on at the bare domain (e.g. `eduid.nl`) *before* they have an account. It explains what
eduID is, lets people install the eduID mobile app, hosts the Terms of Use / Privacy Policy, and
serves a couple of Dutch-service-desk-facing pages. It is **not** where anyone logs in or registers —
those actions link out to the other GUIs (see [Overview](#overview)).

The myconext public gui is built with Vite and to get initially started:

```shell
cd public-gui
yarn install
yarn dev
```

Browse to the [application homepage](http://localhost:3002).

### Build

To deploy production bundles
```bash
mvn deploy
```
### Mail

The default mail configuration sends mails to port 1025. Install https://mailpit.axllent.org/ and capture all emails send. 
You can see all mails delivered at http://localhost:8025/ when mailpit is installed.

In case when not using the Docker Compose file, you can install Mailpit with Brew

```bash
brew install mailpit
```

### Cron

The cron jobs, which may only run on one node, use a database locking mechanisme to obtain a lock. If successful, then the
job is executed, otherwise not. See `myconext.cron.AbstractNodeLeader`

### Crypto

The myconext application uses a private RSA key and corresponding certificate to sign the SAML requests. We don't want
to provide defaults, so in the integration tests the key / certificate pair is generated on the fly. if you want to
deploy the application in an environment where the certificate needs to be registered with the Service Provider (Proxy)
then you can generate a key pair with the following commands:
```
cd myconext/myconext-server/src/main/resources
openssl genrsa -traditional -out myconext.pem 2048
openssl req -subj '/O=Organization, CN=OIDC/' -key myconext.pem -new -x509 -days 365 -out myconext.crt
```
Add the key pair to the [application.yml](myconext-server/src/main/resources/application.yml) file:
```
private_key_path: classpath:/myconext.pem
certificate_path: classpath:/myconext.crt
```
If you need to register the public key in EB then issue this command and copy & paste it in Manage for the correct IdP:
```
cat myconext.crt |ghead -n -1 |tail -n +2 | tr -d '\n'; echo
```
### Translations

The github actions will generate new translations of the source is changed.

```bash
yarn localicious render ./localizations.yaml ./account-gui/src/locale/ --languages en,nl --outputTypes js -c SHARED
rm -fr ./account-gui/src/locale/js/Localizable.ts
yarn localicious render ./localizations.yaml ./myconext-gui/src/locale/ --languages en,nl --outputTypes js -c SHARED
rm -fr ./myconext-gui/src/locale/js/Localizable.ts
```

### Miscellaneous

To get an overview of the git source file's:
```
cloc --read-lang-def=cloc_definitions.txt --vcs=git
```

### Migration

It's possible to migrate from an existing IdP to this IdP. A new identity will be created, and the eppn wil be copied.

### Attribute Manipulation
```
curl -u oidcng:secret "http://login.test2.eduid.nl/myconext/api/attribute-manipulation?sp_entity_id=https://test.okke&uid=0eaa7fb2-4f94-476f-b3f6-c8dfc4115a87&sp_institution_guid=null"
```

### Attribute Aggregation
```
curl -u aa:secret "https://login.test2.eduid.nl/myconext/api/attribute-aggregation?sp_entity_id=https://mijn.test2.eduid.nl/shibboleth&eduperson_principal_name=j.doe@example.com"
```
Endpoint to detect duplicate eduID's for SP's that have the same institutionGuid
```
curl -u aa:secret 'https://login.test2.eduid.nl/myconext/api/system/eduid-duplicates' | jq .
```

### OpenAPI Documentation

http://localhost:8081/myconext/api/swagger-ui/index.html

http://localhost:8081/myconext/api/api-docs

https://login.test2.eduid.nl/myconext/api/swagger-ui/index.html

https://login.test2.eduid.nl/myconext/api/api-docs

### IDIN & e-Herkenning

The redirect URI's for local development have to start with https. You can use the reverse proxy of ngrok for this. For example:
```
ngrok http --domain okke.harsta.eu.ngrok.io 8081
```

### Running the IdP and testing localhost

The [idp_metadata.xml](idp_metadata.xml) file contains the IdP metadata for localhost development. Import an IdP in Manage and
whitelist this for the SP's you want to test with. The OIDC-Playground is capable of testing the different ACR options.

## How to use

Have MyConext server and all 4 GUI projects running.
Note: Account-GUI starts with `Whoops… Something went wrong (404)`, this is ok.

### IDP Flow

1. https://oidc-playground.test2.surfconext.nl/
2. Check `Force authentication` and click on Submit
3. Select `Local eduID IdP` from the list
4. User is `jdoe@example.com`, chose one-time login via e-mail
5. See [Mailpit](http://user:password@145.90.230.133:8025/) for the OTP
6. You get redirected back to the playground with JWT data
