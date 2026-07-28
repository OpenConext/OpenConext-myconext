# Account-GUI (IdP)

The Account-GUI is the SAML/OIDC Identity Provider frontend for MyConext. It is the screen a user
lands on when a Service Provider redirects them to the "Local SURFconext Guest IdP" / "Local eduID
IdP" — it drives the magic-link, password, FIDO2/WebAuthn and Tiqr (mobile app) sign-in flows, as
well as account-linking and step-up/MFA screens.

There is **no home page**: the app only makes sense as the target of an authentication redirect
coming from a Service Provider (e.g. the OIDC-Playground, MyConext-GUI or Servicedesk-GUI). Visiting
it directly without a valid request id will land you on the "Whoops... Something went wrong (404)"
route, which is expected.

## Overview

Account-GUI is one of five modules in this repository (see the [root README](../README.md) for the
full picture):

| Module | Role |
|---|---|
| `myconext-server` | Spring Boot backend: SAML/OIDC IdP logic, persistence, mail | 
| **`account-gui`** | **This module** — the IdP frontend a user authenticates against |
| `myconext-gui` | Example Service Provider used for local development/testing |
| `servicedesk-gui` | Service-desk Service Provider (support staff tooling) |
| `public-gui` | Public content/marketing website |

During local development, `account-gui` runs standalone on port 3000 and proxies its API calls to
`myconext-server` on port 8081 (see [`vite.config.js`](vite.config.js)). In production it is built as
a static bundle and served behind an Apache reverse proxy that forwards API paths to the server (see
[Build & deployment](#build--deployment)).

## Tech stack

Taken from [`package.json`](package.json):

- **[Svelte](https://svelte.dev/) 5.19.3** — component framework. `runes: false` is set in
  [`svelte.config.js`](svelte.config.js), so the codebase uses classic Svelte syntax (`export let`,
  `$:`, etc.), not the newer runes API.
- **[Vite](https://vitejs.dev/) ^8** with `@sveltejs/vite-plugin-svelte` — dev server and bundler.
- **[svelte-routing](https://github.com/EmilTholin/svelte-routing) ^2** — client-side routing (see
  [`src/App.svelte`](src/App.svelte) for the full route table).
- **[i18n-js](https://github.com/fnando/i18n-js)** plus a small custom wrapper
  ([`src/locale/I18n.js`](src/locale/I18n.js)) for translations.
- **[@github/webauthn-json](https://github.com/github/webauthn-json)** — FIDO2/WebAuthn
  registration and authentication.
- **[svelte-qrcode](https://www.npmjs.com/package/svelte-qrcode)** — renders the QR code for Tiqr
  app enrollment.
- **[@friendlycaptcha/sdk](https://www.friendlycaptcha.com/)** — optional captcha, gated by the
  `captchaEnabled`/`captchaSiteKey` config flags (see [Configuration](#configuration)).
- **[dompurify](https://github.com/cure53/DOMPurify)** — sanitizing HTML pulled from translations.
- **[js-cookie](https://github.com/js-cookie/js-cookie)** — reading/writing the `username` and
  `login-preference` cookies used to remember returning users.
- **sass** — component styling.
- **TypeScript** is a dev dependency and [`tsconfig.json`](tsconfig.json) exists, but the source tree
  is almost entirely `.js`/`.svelte`; treat TS support here as partial/legacy.
- **Jest + babel-jest** ([`babel.config.cjs`](babel.config.cjs)) — unit tests, run separately from the
  Vite build.

## Project structure

```
account-gui/
├── index.html              # Vite entry HTML (loads /src/main.js)
├── vite.config.js          # Dev server (port 3000) + API proxy to myconext-server (8081)
├── svelte.config.js        # Svelte preprocessing, runes disabled
├── babel.config.cjs        # Babel preset used by Jest (not by the Vite build)
├── build.sh                # Invoked by Maven during `mvn package` (see below)
├── assembly.xml            # Maven assembly descriptor, zips up dist/
├── pom.xml                 # Maven module wired into the multi-module build
├── docker/
│   ├── Dockerfile           # Apache image serving the built dist/ bundle
│   └── conf/000-default.conf # Apache rewrite rules + ProxyPass to myconextserver:8080
├── public/                  # Static assets copied as-is (fonts, images, styles.css, robots.txt)
└── src/
    ├── main.js               # App bootstrap: polyfills + mounts App.svelte, loads locales
    ├── App.svelte             # Root component: fetches /config, resolves locale, defines routes
    ├── api/index.js           # All fetch calls to myconext-server (see Integration below)
    ├── routes/                # One Svelte component per route/screen (Login, Confirm, WebAuthn, ...)
    ├── components/            # Shared UI building blocks (Header, Footer, Button, Modal, ...)
    ├── stores/                # Svelte writable stores: conf.js, user.js, domains.js
    ├── constants/             # Enums: AlertType, loginStatus, enrollmentStatus, cookieNames, regexp
    ├── locale/                # I18n.js/I18nRemote.js + en.js/nl.js + generated locale/js/{en,nl}
    ├── verify/                # ID-verification (iDIN bank chooser, servicedesk verify flow) screens
    ├── icons/, img/           # SVG icons and images
    └── __tests__/             # Jest unit tests (locale, utils, validation)
```

## Prerequisites

- Node.js `24.3.0` — pinned in [`.nvmrc`](.nvmrc); run `nvm use` before installing. (Note: the root
  README currently lists NodeJS 23.2.0 as the system requirement — the version actually pinned for
  this module is 24.3.0.)
- Yarn (Yarn 1.x per the root README)
- A running `myconext-server` instance on `http://localhost:8081`, since `yarn dev` proxies all API
  calls there (see [`vite.config.js`](vite.config.js)).

## Getting started

```shell
cd account-gui
nvm use
yarn install
yarn dev
```

The dev server starts on **http://localhost:3000** and opens automatically. There is no home page —
you need to initiate a login from a Service Provider (e.g. the MyConext-GUI SP, or an OIDC/SAML
client such as the OIDC-Playground) and choose "Local SURFconext Guest IdP" / "Local eduID IdP" to be
redirected here.

## Available scripts

From [`package.json`](package.json):

| Script | Command | Description |
|---|---|---|
| `yarn dev` | `vite` | Start the Vite dev server on port 3000 with API proxying |
| `yarn build` | `vite build` | Produce a production bundle in `dist/` |
| `yarn preview` | `vite preview` | Serve the built `dist/` bundle locally |
| `yarn lint` | `eslint .` | Lint the source tree — note: no ESLint config file (`.eslintrc*` /
  `eslint.config.js`) currently exists in this directory or the repo root, so this script will fail
  until one is added |
| `yarn test` | `jest src` | Run Jest unit tests under `src/__tests__` |
| `yarn test:watch` | `npm run test -- --watch` | Run Jest in watch mode |

## Configuration

There is no `.env`/`.env.example` file. Instead, the app fetches its runtime configuration from the
backend at startup:

- On mount, `App.svelte` calls `configuration()` (`GET /config`, proxied to `myconext-server`) and
  stores the result in the `conf` writable store ([`src/stores/conf.js`](src/stores/conf.js)). This
  includes feature flags such as `featureWebAuthn`, `featureAllowList`,
  `featureWarningEducationalEmailDomain`, `featureIdVerify`, `featureServiceDeskActive`, and captcha
  settings (`captchaEnabled`, `captchaSiteKey`), plus URLs like `magicLinkUrl`,
  `myconextWebAuthnRedirectUrl`, `accountBaseUrl` and `mobileAppRedirect`.
- Locale is resolved from the `lang` query parameter, then the `lang` cookie, then the browser
  language, defaulting to English (`en`/`nl` supported).
- Returning-user state (`username`, `login-preference` cookies) is read via `js-cookie`
  (see [`src/constants/cookieNames.js`](src/constants/cookieNames.js)).

Build-time proxy targets (dev only) are configured directly in
[`vite.config.js`](vite.config.js) and point at `http://localhost:8081`.

## Integration with myconext-server

All backend calls live in [`src/api/index.js`](src/api/index.js) and go through a shared
`validFetch` helper that sets `credentials: same-origin`, a manual redirect mode (to detect
"opaqueredirect" responses and force a reload), an `Accept-Language` header driven by the current
locale, and an `X-CSRF-TOKEN` header captured from the previous response.

Key endpoint groups called from the GUI:

- **Login/registration**: `/myconext/api/idp/generate_code_request` (POST for a new user, PUT for an
  existing user or password login), `/myconext/api/idp/verify_code_request`,
  `/myconext/api/idp/resend_code_request`.
- **Email domain checks**: `/myconext/api/idp/email/domain/institutional`,
  `/myconext/api/idp/email/domain/allowed`.
- **User/session info**: `/myconext/api/idp/me/:hash`, `/myconext/api/idp/service/name/:id`,
  `/myconext/api/idp/service/hash/:hash`, `/myconext/api/idp/service/email`.
- **WebAuthn/FIDO2**: `/myconext/api/idp/security/webauthn/registration`,
  `/myconext/api/idp/security/webauthn/authentication` (POST to start, PUT to complete), built on top
  of `@github/webauthn-json`.
- **Tiqr (mobile app)**: `/tiqr/start-enrollment`, `/tiqr/poll-enrollment`, `/tiqr/qrcode`,
  `/tiqr/generate-backup-code`, `/tiqr/send-phone-code`, `/tiqr/verify-phone-code`,
  `/tiqr/start-authentication`, `/tiqr/poll-authentication`, `/tiqr/remember-me`,
  `/tiqr/manual-response`.
- **ID verification (iDIN)**: `/myconext/api/sp/idin/issuers` (see [`src/verify/`](src/verify/)).
- **App config**: `GET /config` (see [Configuration](#configuration)).
- **Client-side error reporting**: `/myconext/api/sp/error`, also used as the fallback when a
  translation key is missing (see [`src/locale/I18n.js`](src/locale/I18n.js)).

In development these paths are proxied by Vite to `myconext-server` on port 8081
([`vite.config.js`](vite.config.js)). In production/docker they are proxied by Apache to
`myconextserver:8080` (see [`docker/conf/000-default.conf`](docker/conf/000-default.conf)).

Successful magic-link/step-up flows finish via [`src/utils/sso.js`](src/utils/sso.js), which
redirects the browser back to the Service Provider's `redirect` URL (validated against
`magicLinkUrlPrefix` to prevent open-redirect abuse), appending the session hash.

## Translations

Translations are generated by the `localicious` tool from the shared `localizations.yaml` at the
repo root (documented in the [root README](../README.md#translations)):

```bash
yarn localicious render ./localizations.yaml ./account-gui/src/locale/ --languages en,nl --outputTypes js -c SHARED
```

This regenerates [`src/locale/js/en/strings.json`](src/locale/js) and
`src/locale/js/nl/strings.json`, which are imported by the legacy [`src/locale/en.js`](src/locale/en.js)
/ [`src/locale/nl.js`](src/locale/nl.js) + [`I18nRemote.js`](src/locale/I18nRemote.js) fallback layer
in [`I18n.js`](src/locale/I18n.js). Missing keys are reported back to the server via the
`/myconext/api/sp/error` endpoint rather than failing silently.

## Testing

```shell
yarn test
```

Runs Jest (`jest src`) against [`src/__tests__/`](src/__tests__), which currently covers:

- `locale/en.test.js` — sanity-checks the English locale strings
- `utils/date.test.js` — date-formatting utilities
- `validation/regexp.test.js` — the regular expressions in
  [`src/constants/regexp.js`](src/constants/regexp.js)

Jest transforms are configured via [`babel.config.cjs`](babel.config.cjs) (`@babel/preset-env` with
`core-js` polyfills); this Babel config is only used for the test pipeline, not for the Vite dev/build
pipeline.

## Build & deployment

Production bundles are produced by Vite (`vite build` → `dist/`) but are normally triggered through
Maven, not invoked directly:

1. `mvn package`/`mvn deploy` at the repo root runs the `exec-maven-plugin` configured in
   [`pom.xml`](pom.xml), which executes [`build.sh`](build.sh):
   ```bash
   rm -Rf public/bundle* dist/* target/*
   nvm use
   yarn install --force && yarn test && yarn build
   ```
   (so a full Maven build also runs the Jest test suite before bundling).
2. The `maven-assembly-plugin` then zips the resulting `dist/` directory per
   [`assembly.xml`](assembly.xml) into the module's build artifact.
3. For container deployment, [`docker/Dockerfile`](docker/Dockerfile) copies `dist/` into an Apache
   base image (`ghcr.io/openconext/openconext-basecontainers/apache2`), configured by
   [`docker/conf/000-default.conf`](docker/conf/000-default.conf), which serves the SPA (falling back
   to `index.html` for client-side routes) and reverse-proxies `/myconext/api`, `/tiqr`,
   `/saml/guest-idp`, `/config`, `/register`, `/servicedesk`, `/doLogin`, `/doLogout`,
   `/create-from-institution-login`, `/actuator` and `/internal` to `myconextserver:8080`.

Note: [`manifest.json`](manifest.json) at the module root still contains boilerplate from a
`preact-example` template (name, icons pointing at non-existent `/assets/icons/...` files) — it does
not reflect this Svelte application and looks stale.

## Related modules

- [Root README](../README.md) — overall system, System Requirements, running MongoDB/Mailpit,
  the full IdP flow walkthrough
- [`myconext-server`](../myconext-server) — the Spring Boot backend this GUI talks to
- [`myconext-gui`](../myconext-gui) — example Service Provider for local testing (no README yet)
- [`servicedesk-gui`](../servicedesk-gui) — service-desk Service Provider
- [`public-gui`](../public-gui) — public content website

---
_Last updated: 2026-07-28_
