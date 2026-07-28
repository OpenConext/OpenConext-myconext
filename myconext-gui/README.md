# MyConext-GUI (SP)

MyConext-GUI is the "My eduID" self-service Service Provider frontend for MyConext. Once a user is
authenticated (via [`account-gui`](../account-gui)), this is where they land to manage their own
eduID identity: personal info, linked (institution/external) accounts, security methods (password,
FIDO2/WebAuthn, the Tiqr mobile app), connected services and account deletion.

Like `account-gui`, this app has no meaningful anonymous landing page for most routes: on mount it
calls `/myconext/api/sp/me`, and if the user isn't authenticated it redirects to the configured
`loginUrl` (see [`src/App.svelte`](src/App.svelte)). A small set of routes (`/create-from-institution`,
`/landing`, `/install-app`) are reachable without an existing session, to support the "create an
eduID linked to your institution account" flow for guests.

## Overview

MyConext-GUI is one of five modules in this repository (see the [root README](../README.md) for the
full picture):

| Module | Role |
|---|---|
| `myconext-server` | Spring Boot backend: SAML/OIDC IdP logic, persistence, mail |
| `account-gui` | The IdP frontend a user authenticates against |
| **`myconext-gui`** | **This module** — the "My eduID" self-service Service Provider |
| `servicedesk-gui` | Service-desk Service Provider (support staff tooling) |
| `public-gui` | Public content/marketing website |

During local development, `myconext-gui` runs standalone on port 3001 and proxies its API calls to
`myconext-server` on port 8081 (see [`vite.config.js`](vite.config.js)). In production it is built as
a static bundle and served behind an Apache reverse proxy that forwards API/auth paths to the server
(see [Build & deployment](#build--deployment)).

## Tech stack

Taken from [`package.json`](package.json):

- **[Svelte](https://svelte.dev/) 5.19.3** — component framework. There is no `runes` setting in
  [`svelte.config.js`](svelte.config.js) and the source tree uses classic Svelte syntax throughout
  (`export let`, no `$state`/`$derived`/`$props()` found in `src/**/*.svelte`).
- **[Vite](https://vitejs.dev/) ^8** with `@sveltejs/vite-plugin-svelte` — dev server and bundler.
- **[svelte-routing](https://github.com/EmilTholin/svelte-routing) ^2** — client-side routing. The
  top-level route table lives in [`src/App.svelte`](src/App.svelte); almost every authenticated route
  maps to `<Home bookmark="...">`, and `Home.svelte` renders one of ~30 tab/sub-screen components
  based on that `bookmark` prop (see [`src/routes/Home.svelte`](src/routes/Home.svelte)).
- **[i18n-js](https://github.com/fnando/i18n-js)** plus a small custom wrapper
  ([`src/locale/I18n.js`](src/locale/I18n.js)) for translations, with a legacy fallback layer
  ([`src/locale/I18nRemote.js`](src/locale/I18nRemote.js)).
- **[@github/webauthn-json](https://github.com/github/webauthn-json)** — FIDO2/WebAuthn credential
  registration, used from [`src/routes/WebAuthn.svelte`](src/routes/WebAuthn.svelte) /
  [`src/routes/Credential.svelte`](src/routes/Credential.svelte).
- **[svelte-qrcode](https://www.npmjs.com/package/svelte-qrcode)** — renders the QR code for Tiqr app
  enrollment in [`src/routes/tiqr/GetApp.svelte`](src/routes/tiqr/GetApp.svelte). Note:
  `@castlenine/svelte-qrcode` is also listed as a devDependency in `package.json` but is not imported
  anywhere under `src/`; it appears unused.
- **[svelte-select](https://github.com/rob-balfre/svelte-select)** — used in
  [`src/verify/ServiceDesk.svelte`](src/verify/ServiceDesk.svelte) (declared as a devDependency in
  `package.json` even though it's used at runtime).
- **[dompurify](https://github.com/cure53/DOMPurify)** — sanitizing HTML pulled from translations.
- **[js-cookie](https://github.com/js-cookie/js-cookie)** — reading the `lang` cookie used to resolve
  the active locale.
- **sass** — component styling.
- **TypeScript**: [`tsconfig.json`](tsconfig.json) exists, but the source tree is entirely
  `.js`/`.svelte`; treat TS support here as configuration-only/unused.
- **Jest + babel-jest** ([`babel.config.cjs`](babel.config.cjs)) — unit tests, run separately from the
  Vite build.

## Project structure

```
myconext-gui/
├── index.html               # Vite entry HTML (loads /src/main.js)
├── vite.config.js           # Dev server (port 3001) + API proxy to myconext-server (8081)
├── svelte.config.js         # Svelte preprocessing (vitePreprocess only)
├── babel.config.cjs         # Babel preset used by Jest (not by the Vite build)
├── build.sh                 # Invoked by Maven during `mvn package` (see below)
├── assembly.xml             # Maven assembly descriptor, zips up dist/
├── pom.xml                  # Maven module wired into the multi-module build
├── manifest.json            # PWA manifest — still boilerplate from a "preact-example" template
├── docker/
│   ├── Dockerfile            # Apache image serving the built dist/ bundle
│   └── conf/000-default.conf # Apache rewrite rules + ProxyPass to myconextserver:8080
├── public/                   # Static assets copied as-is (favicon, styles.css, robots.txt, img/)
└── src/
    ├── main.js                # App bootstrap: polyfills + mounts App.svelte, loads locale/en.js, nl.js
    ├── App.svelte              # Root component: fetches /myconext/api/sp/me + /config, defines routes
    ├── api/index.js            # All fetch calls to myconext-server (see Integration below)
    ├── routes/                 # One Svelte component per screen (Home is the tabbed shell)
    │   └── tiqr/                # Tiqr mobile-app enrollment/recovery/deactivation screens
    ├── components/              # Shared UI building blocks (Header, Footer, Button, Modal, Flash, ...)
    ├── verify/                  # ID-verification screens (ServiceDesk verify choice, bank chooser)
    ├── stores/                  # Svelte writable stores: user.js (user + config), domains.js
    ├── constants/               # Enums: loginStatus, enrollmentStatus, authenticationStatus
    ├── utils/                   # utils.js, poll.js, date.js, services.js (non-API helper logic)
    ├── format/                  # date.js formatting helpers
    ├── validation/              # regexp.js
    ├── locale/                  # I18n.js/I18nRemote.js + en.js/nl.js + generated locale/js/{en,nl}
    ├── icons/                   # SVG icons (incl. icons/redesign, icons/verify, icons/remotecreation)
    └── __tests__/               # Jest unit tests (init, locale, utils, format, validation)
```

## Prerequisites

- Node.js `24.3.0` — pinned in [`.nvmrc`](.nvmrc); run `nvm use` before installing. (Note: the root
  README currently lists NodeJS 23.2.0 as the system requirement — the version actually pinned for
  this module is 24.3.0, matching `account-gui`.)
- Yarn (Yarn 1.x per the root README)
- A running `myconext-server` instance on `http://localhost:8081`, since `yarn dev` proxies
  `/myconext/api`, `/config`, `/register`, `/doLogout` and `/tiqr` there (see
  [`vite.config.js`](vite.config.js)).

## Getting started

```shell
cd myconext-gui
nvm use
yarn install
yarn dev
```

The dev server starts on **http://localhost:3001** and opens automatically. Most routes require an
authenticated session; without one you'll be redirected to the IdP login URL returned by `/config`
(normally `account-gui`).

## Available scripts

From [`package.json`](package.json):

| Script | Command | Description |
|---|---|---|
| `yarn dev` | `vite` | Start the Vite dev server on port 3001 with API proxying |
| `yarn build` | `vite build` | Produce a production bundle in `dist/` |
| `yarn preview` | `vite preview` | Serve the built `dist/` bundle locally |
| `yarn lint` | `eslint .` | Lint the source tree — note: no ESLint config file (`.eslintrc*` /
  `eslint.config.js`) currently exists in this directory or the repo root, so this script will fail
  until one is added |
| `yarn test` | `jest src` | Run Jest unit tests under `src/__tests__` |
| `yarn test:watch` | `npm run test -- --watch` | Run Jest in watch mode |

## Configuration

There is no `.env`/`.env.example` file. Instead, the app fetches its runtime configuration from the
backend at startup, in [`src/App.svelte`](src/App.svelte):

- `configuration()` (`GET /config`, proxied to `myconext-server`) populates the `config` writable
  store ([`src/stores/user.js`](src/stores/user.js)). This includes the IdP `loginUrl`,
  `myConextUrlGuestIdp`, `accountBaseUrl`/`accountWebAuthUrl`, `magicLinkUrl`, feature flags
  (`featureWebAuthn`, `featureOidcTokenAPI`, `featureConnections`, `featureAllowList`,
  `featureDefaultRememberMe`, `featureWarningEducationalEmailDomain`, `featureIdVerify`,
  `featureServiceDeskActive`, `createEduIDInstitutionEnabled`), `expirationNonValidatedDurationDays`,
  and `enableAccountLinking`/`useApp` toggles.
- If `config.isAuthenticated === false` and the current path isn't one of the unprotected routes
  (`/create-from-institution`, `/landing`, `/install-app`), the app stores the current path in the
  `redirectPath` store and redirects to `config.loginUrl` via
  [`src/utils/utils.js`](src/utils/utils.js) `redirectToLogin`.
- Otherwise `me()` (`GET /myconext/api/sp/me`) populates the `user` writable store with the
  authenticated user's profile (name, email, linked accounts, credentials, OIDC tokens, etc.).
- Locale is resolved from the `lang` query parameter, then the `lang` cookie (via `js-cookie`), then
  the browser language, defaulting to English; it's then overridden by the user's
  `preferredLanguage` once `me()` resolves.

## Integration with myconext-server

All backend calls live in [`src/api/index.js`](src/api/index.js) and go through a shared
`validFetch` helper that sets `credentials: same-origin`, a manual redirect mode (to detect
`"opaqueredirect"` responses and force a reload), an `Accept-Language` header driven by the current
locale, and an `X-CSRF-TOKEN` header captured from the previous response.

Key endpoint groups called from the GUI:

- **Session/config**: `GET /myconext/api/sp/me`, `GET /config`.
- **Profile updates**: `PUT /myconext/api/sp/update`, `PUT /myconext/api/sp/lang`,
  `PUT /myconext/api/sp/prefer-linked-account`.
- **Email change**: `PUT /myconext/api/sp/generate-email-code`,
  `GET /myconext/api/sp/resend-email-code`, `PUT /myconext/api/sp/verify-email-code`,
  `GET /myconext/api/sp/confirm-email`.
- **Password**: `PUT /myconext/api/sp/generate-password-code` (as a `PUT` despite the name),
  `GET /myconext/api/sp/resend-password-code`, `PUT /myconext/api/sp/verify-password-code`,
  `PUT /myconext/api/sp/update-password`, `GET /myconext/api/sp/password-reset-hash-valid`.
- **Security/credentials**: `GET /myconext/api/sp/security/webauthn` (start WebAuthn flow),
  `POST`/`PUT /myconext/api/sp/credential` (add/delete a public key credential), built on top of
  `@github/webauthn-json`.
- **Account/linked accounts**: `DELETE /myconext/api/sp/delete` (delete account, then logs out),
  `PUT /myconext/api/sp/institution` (delete a linked account), `GET /myconext/api/sp/oidc/link`
  (start link-account flow), `GET /myconext/api/sp/verify/link` + `GET /myconext/api/sp/idin/issuers`
  (ID-verify/iDIN flow, see [`src/verify/`](src/verify/)).
- **Services/tokens**: `PUT /myconext/api/sp/service` (delete a service + its tokens),
  `PUT /myconext/api/sp/tokens` / `GET /myconext/api/sp/tokens` (delete/list OIDC tokens).
- **Tiqr (mobile app)**: `GET /tiqr/sp/start-enrollment`, `GET /tiqr/sp/finish-enrollment`,
  `GET /tiqr/poll-enrollment`, `GET /tiqr/sp/generate-backup-code` (and `re-generate-backup-code`),
  `POST /tiqr/sp/send-phone-code` (and `re-send-phone-code`),
  `POST /tiqr/sp/verify-phone-code` (and `re-verify-phone-code`),
  `POST /tiqr/sp/deactivate-app`, `GET /tiqr/sp/send-deactivation-phone-code`,
  `POST /tiqr/sp/start-authentication`, `GET /tiqr/sp/poll-authentication`,
  `POST /tiqr/sp/manual-response`.
- **Create-from-institution** (guest flow): `GET /myconext/api/sp/create-from-institution`,
  `POST /myconext/api/sp/create-from-institution/email`,
  `GET /myconext/api/sp/create-from-institution/info`,
  `PUT /myconext/api/sp/create-from-institution/verify`,
  `GET /myconext/api/sp/create-from-institution/resendMail`, plus domain-check endpoints
  `GET /myconext/api/sp/create-from-institution/domain/{institutional,allowed}`.
- **Logout**: `DELETE /myconext/api/sp/forget` followed by `GET /myconext/api/sp/logout`.
- **Client-side error reporting**: `POST /myconext/api/sp/error`, also used as the fallback when a
  translation key is missing (see [`src/locale/I18n.js`](src/locale/I18n.js)).
- **Diagnostics**: `GET /myconext/api/sp/testWebAuthnUrl`.

In development these paths are proxied by Vite to `myconext-server` on port 8081
([`vite.config.js`](vite.config.js) — note only `/myconext/api`, `/config`, `/register`, `/doLogout`
and `/tiqr` are proxied there; the OAuth2 paths below aren't needed in dev). In production/docker they
are proxied by Apache to `myconextserver:8080`, together with `/oauth2/authorization`,
`/login/oauth2` and `/auth/login` (see [`docker/conf/000-default.conf`](docker/conf/000-default.conf)).

## Translations

Translations are generated by the `localicious` tool from the shared `localizations.yaml` at the
repo root (documented in the [root README](../README.md#translations)):

```bash
yarn localicious render ./localizations.yaml ./myconext-gui/src/locale/ --languages en,nl --outputTypes js -c SHARED
```

This regenerates `src/locale/js/en/strings.json` and `src/locale/js/nl/strings.json`, which are
imported by [`src/locale/I18n.js`](src/locale/I18n.js). Legacy translations still live in
[`src/locale/en.js`](src/locale/en.js) / [`src/locale/nl.js`](src/locale/nl.js) and are consulted via
the [`src/locale/I18nRemote.js`](src/locale/I18nRemote.js) fallback layer when a key is missing from
the generated JSON. Missing keys are reported back to the server via the
`/myconext/api/sp/error` endpoint rather than failing silently.

## Testing

```shell
yarn test
```

Runs Jest (`jest src`) against [`src/__tests__/`](src/__tests__), which currently covers:

- `init.js` — sanity-checks that `I18n`, `en` and `nl` load correctly
- `locale/en.test.js` — checks the English locale strings
- `utils/date.test.js` and `format/date.test.js` — date-formatting utilities
- `utils/utils.test.js` — general helpers from [`src/utils/utils.js`](src/utils/utils.js)
- `validation/regexp.test.js` — the regular expressions in
  [`src/validation/regexp.js`](src/validation/regexp.js)

Jest transforms are configured via [`babel.config.cjs`](babel.config.cjs) (`@babel/preset-env` with
`core-js` polyfills, targeting `last 2 versions, ie >= 11`); this Babel config is only used for the
test pipeline, not for the Vite dev/build pipeline.

## Build & deployment

Production bundles are produced by Vite (`vite build` → `dist/`) but are normally triggered through
Maven, not invoked directly:

1. `mvn package`/`mvn deploy` at the repo root runs the `exec-maven-plugin` configured in
   [`pom.xml`](pom.xml), which executes [`build.sh`](build.sh):
   ```bash
   rm -Rf public/bundle*
   rm -Rf dist/*
   rm -Rf target/*
   nvm use
   yarn install --force && yarn test && yarn build
   ```
   (so a full Maven build also runs the Jest test suite before bundling).
2. The `maven-assembly-plugin` then zips the resulting `dist/` directory (including
   `.well-known/apple-app-site-association`, used for iOS universal links to the Tiqr app) per
   [`assembly.xml`](assembly.xml) into the module's build artifact.
3. For container deployment, [`docker/Dockerfile`](docker/Dockerfile) copies `dist/` into an Apache
   base image (`ghcr.io/openconext/openconext-basecontainers/apache2`), configured by
   [`docker/conf/000-default.conf`](docker/conf/000-default.conf), which serves the SPA (falling back
   to `index.html` for client-side routes) and reverse-proxies `/myconext/api`, `/tiqr`,
   `/oauth2/authorization`, `/login/oauth2`, `/auth/login`, `/doLogout`, `/internal`, `/actuator`,
   `/robots.txt` and `/config` to `myconextserver:8080`. It also sets `X-Frame-Options: DENY`,
   `Referrer-Policy: same-origin` and `X-Content-Type-Options: nosniff`.

Note: [`manifest.json`](manifest.json) at the module root still contains boilerplate from a
`preact-example` template (name `"preact-example"`, icons pointing at non-existent
`/assets/icons/...` files) — it does not reflect this Svelte application and looks stale, mirroring
the same issue already noted in `account-gui`'s `manifest.json`.

## Related modules

- [Root README](../README.md) — overall system, System Requirements, running MongoDB/Mailpit,
  the full IdP flow walkthrough
- [`myconext-server`](../myconext-server) — the Spring Boot backend this GUI talks to
- [`account-gui`](../account-gui) — the IdP frontend users authenticate against before landing here
- [`servicedesk-gui`](../servicedesk-gui) — service-desk Service Provider
- [`public-gui`](../public-gui) — public content website

---
_Last updated: 2026-07-28_
