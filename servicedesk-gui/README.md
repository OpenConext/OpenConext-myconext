# ServiceDesk-GUI (SP)

ServiceDesk-GUI is the internal tool SURF/SURFconext service-desk staff use to perform **in-person
identity verification** for eduID users. A student (or other eduID user) who needs a formally
verified identity generates a numeric verification code in the eduID app, visits (or calls) the
service desk, and a service-desk employee uses this application to: look up the code, manually
check the person's ID document against the data on file, and approve the check — after which the
person's identity is marked as verified in eduID.

There is no self-service function here: every route except `/login` requires an authenticated,
authorized service-desk employee (see [Overview](#overview)).

## Overview

ServiceDesk-GUI is one of the (currently four, per the [root README](../README.md)) frontend/backend
modules in this repository:

| Module | Role |
|---|---|
| `myconext-server` | Spring Boot backend: SAML/OIDC IdP logic, persistence, mail, and the `/myconext/api/servicedesk/*` endpoints this GUI calls |
| `account-gui` | The IdP frontend a user authenticates against |
| `myconext-gui` | Example Service Provider used for local development/testing |
| **`servicedesk-gui`** | **This module** — internal SP used by service-desk staff to verify user identities |
| `public-gui` | Public content/marketing website |

During local development, `servicedesk-gui` runs standalone on port **3003** and proxies its API
calls to `myconext-server` on port 8081 (see [`vite.config.js`](vite.config.js)). In production it is
built as a static bundle and served behind an Apache reverse proxy (see
[Build & deployment](#build--deployment)).

Access control has two layers, both enforced against data from `myconext-server`:

1. **Authentication** — visiting the app while unauthenticated shows the [`Login`](src/pages/Login.jsx)
   landing page. Clicking "Log in" redirects the browser to `config.loginUrlServiceDesk` (an OAuth2/OIDC
   login flow on `myconext-server`, with `registration_id=service_desk`), not a client-side login form.
2. **Authorization** — once authenticated, [`App.jsx`](src/App.jsx) calls
   `GET /myconext/api/servicedesk/me`. Only users whose account has `serviceDeskMember: true` (see
   `myconext-server`'s [`ExternalUser`](../myconext-server/src/main/java/myconext/model/ExternalUser.java))
   reach the app; everyone else is routed to the [`Forbidden`](src/pages/Forbidden.jsx) page.

**Note on the root README:** the [root README](../README.md#servicedesk-gui-sp) currently describes
this module as "also built with Svelte", but the source tree here is actually **React 19 + Vite**
(JSX, not `.svelte` files) — that line in the root README appears to be stale/copied from the
`account-gui`/`myconext-gui` sections.

## Tech stack

Taken from [`package.json`](package.json):

- **[React](https://react.dev/) 19.2** + **react-dom 19.2** — component framework (function
  components + hooks throughout; no class components).
- **[Vite](https://vitejs.dev/) ^8** with **[@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react)**
  — dev server and bundler.
- **[vite-plugin-svgr](https://react-svgr.com/)** — imports `.svg` files as React components (e.g.
  `import FrontDesk from "../icons/frontdesk.svg"` then `<FrontDesk/>`), configured with
  `exportType: "default"`, `ref: true`, `svgo: false` in [`vite.config.js`](vite.config.js).
- **[react-router-dom](https://reactrouter.com/) 7** — client-side routing (see
  [`src/App.jsx`](src/App.jsx) for the route table and [`src/main.jsx`](src/main.jsx) for the
  top-level `BrowserRouter`).
- **[zustand](https://github.com/pmndrs/zustand) 5** — the single app store,
  [`src/stores/AppStore.js`](src/stores/AppStore.js) (`user`, `config`, `controlCode`, `flash`,
  `breadcrumbPath`, `csrfToken`).
- **[@surfnet/sds](https://www.npmjs.com/package/@surfnet/sds)** — the SURFconext design-system
  component library (`Button`, `Loader`, `Switch`, `Toaster`, `CodeValidation`, `Chip`, `Logo`,
  `UserInfo`, `Alert`, `ErrorIndicator`, ...). Its stylesheets are imported first in
  [`src/main.jsx`](src/main.jsx), with a comment warning not to reorder the imports.
- **[dompurify](https://github.com/cure53/DOMPurify)** — sanitizing HTML pulled from translations
  before using `dangerouslySetInnerHTML` (used throughout `src/pages`, `src/tabs`,
  `src/components`). `isomorphic-dompurify` is also declared as a dependency but is not imported
  directly anywhere in `src`.
- **[i18n-js](https://github.com/fnando/i18n-js)** — translations, wrapped by
  [`src/locale/I18n.js`](src/locale/I18n.js).
- **[react-datepicker](https://reactdatepicker.com/)** — the date-of-birth picker used in the
  [`Control`](src/tabs/Control.jsx) verification step.
- **[react-tooltip](https://react-tooltip.com/)** — tooltip styles imported in `main.jsx` (not
  otherwise exercised in the current components).
- **[js-cookie](https://github.com/js-cookie/js-cookie)** — reading/writing the `lang` cookie (see
  [`LanguageSelector`](src/components/LanguageSelector.jsx)).
- **sass** — component-scoped `.scss` stylesheets (one per component/page, co-located).
- **[ESLint](https://eslint.org/) 9** flat config ([`eslint.config.js`](eslint.config.js)) with
  `eslint-plugin-react`, `eslint-plugin-react-hooks`, `eslint-plugin-react-refresh`.
- **[Vitest](https://vitest.dev/)** — unit tests (not Jest, unlike `account-gui`).

## Project structure

```
servicedesk-gui/
├── index.html               # Vite entry HTML (loads /src/main.jsx)
├── vite.config.js           # Dev server (port 3003) + API proxy to myconext-server (8081)
├── eslint.config.js         # Flat ESLint config for React
├── build.sh                 # Invoked by Maven during `mvn package` (see below)
├── assembly.xml             # Maven assembly descriptor, zips up dist/
├── pom.xml                  # Maven module wired into the multi-module build
├── docker/
│   ├── Dockerfile            # Apache image serving the built dist/ bundle
│   └── conf/000-default.conf # Apache rewrite rules + ProxyPass to myconextserver:8080
├── public/
│   └── favicon.ico
└── src/
    ├── main.jsx               # App bootstrap: mounts <App/> inside a BrowserRouter, loads SDS styles
    ├── App.jsx                # Auth/authorization gate + top-level route table
    ├── api/index.js           # All fetch calls to myconext-server
    ├── pages/                 # Login, Home, Forbidden, NotFound, RefreshRoute
    ├── tabs/                  # The 3-step verification wizard: Verification → Control → Confirmation
    ├── components/            # Header, Footer, UserMenu, LandingInfo, BreadCrumb, Flash,
    │                          # LanguageSelector, Page, Tab(s), UnitHeader
    ├── stores/AppStore.js      # zustand store (user, config, controlCode, flash, breadcrumbPath)
    ├── locale/                 # I18n.js wrapper + en.js/nl.js translation bundles
    ├── utils/                  # Utils.js (isEmpty, sanitizeURL, distinctValues, stopEvent),
    │                          # QueryParameters.js
    ├── icons/                  # SVG icons (imported as components via vite-plugin-svgr) and the
    │                          # chatgpt/ subfolder of AI-generated illustration images used on the
    │                          # landing page
    └── __tests__/              # Vitest unit tests (locale, store, utils)
```

## Prerequisites

- Node.js `24.3.0` — pinned in [`.nvmrc`](.nvmrc); run `nvm use` before installing.
- Yarn (Yarn 1.x per the [root README](../README.md)).
- A running `myconext-server` instance on `http://localhost:8081`, since `yarn dev` proxies all API
  calls there (see [`vite.config.js`](vite.config.js)).

## Getting started

```shell
cd servicedesk-gui
nvm use
yarn install
yarn dev
```

The dev server starts on **http://localhost:3003** and opens automatically. Unauthenticated visitors
land on the marketing/login page ([`src/pages/Login.jsx`](src/pages/Login.jsx)); logging in requires
a `myconext-server` account flagged as a service-desk member (see [Overview](#overview)).

## Available scripts

From [`package.json`](package.json):

| Script | Command | Description |
|---|---|---|
| `yarn dev` | `vite` | Start the Vite dev server on port 3003 with API proxying |
| `yarn build` | `vite build` | Produce a production bundle in `dist/` |
| `yarn lint` | `eslint .` | Lint the source tree using [`eslint.config.js`](eslint.config.js) |
| `yarn preview` | `vite preview` | Serve the built `dist/` bundle locally |
| `yarn test` | `vitest` | Run the Vitest suite under `src/__tests__/` (watch mode locally; `build.sh` sets `CI=true` so it runs once during a Maven build) |

## Configuration

There is no `.env`/`.env.example` file. Instead, the app fetches its runtime configuration from the
backend at startup:

- `App.jsx` calls `configuration()` (`GET /config`, proxied to `myconext-server`) and stores the
  result in the zustand `config` state ([`src/stores/AppStore.js`](src/stores/AppStore.js)). Known
  keys used by the GUI: `isAuthenticated`, `loginUrlServiceDesk` (used by
  [`Login.jsx`](src/pages/Login.jsx) to build the OAuth2 login redirect) and `accountBaseUrl` (used
  by [`Forbidden.jsx`](src/pages/Forbidden.jsx) to build the logout redirect).
- Locale is resolved in [`src/locale/I18n.js`](src/locale/I18n.js) from the `lang` query parameter,
  then the `lang` cookie, then the browser language, defaulting to English (`en`/`nl` supported).
- The `AppStore` also exposes a `csrfToken` field that [`src/api/index.js`](src/api/index.js) sends
  as an `X-CSRF-TOKEN` header on every request — but nothing in the current app code ever calls
  `useAppStore.setState({csrfToken: ...})` outside of
  [`src/__tests__/stores/AppStore.test.js`](src/__tests__/stores/AppStore.test.js), so in practice
  this header is sent as `undefined` at runtime.
- Build-time proxy targets (dev only) are configured directly in
  [`vite.config.js`](vite.config.js) and point at `http://localhost:8081` for `/myconext/api`,
  `/config` and `/doLogout`.

## Integration with myconext-server

All backend calls live in [`src/api/index.js`](src/api/index.js), routed through a shared
`validFetch`/`fetchJson` helper that sets `credentials: same-origin`, manual redirect handling (to
detect `opaqueredirect` responses and force a reload), and an `Accept-Language` header driven by the
current locale.

Endpoints called from the GUI (all served by
[`ServiceDeskController`](../myconext-server/src/main/java/myconext/api/ServiceDeskController.java) under
`/myconext/api/servicedesk`, except `/config` which is a top-level endpoint):

| GUI call | Endpoint | Used by |
|---|---|---|
| `configuration()` | `GET /config` | `App.jsx`, `Login.jsx` — runtime config |
| `me()` | `GET /myconext/api/servicedesk/me` | `App.jsx` — fetches the authenticated user and its `serviceDeskMember` flag |
| `logout()` | `GET /myconext/api/servicedesk/logout` | `UserMenu.jsx`, `Forbidden.jsx` |
| `getUserControlCode(code)` | `GET /myconext/api/servicedesk/user/:code` | [`Verification.jsx`](src/tabs/Verification.jsx) — step 1, look up the code the user generated in eduID |
| `validateDate(dayOfBirth)` | `GET /myconext/api/servicedesk/validate?dayOfBirth=...` | [`Control.jsx`](src/tabs/Control.jsx) — step 2, checks whether the stored date of birth is a valid, parseable date |
| `convertUserControlCode(...)` | `PUT /myconext/api/servicedesk/approve` | [`Control.jsx`](src/tabs/Control.jsx) — step 2 submit, approves the identity check and links the account |

The verification flow itself is a 3-step wizard driven by
[`src/tabs/VerifyWizard.jsx`](src/tabs/VerifyWizard.jsx):

1. **[`Verification`](src/tabs/Verification.jsx)** — employee enters the 5-digit code the eduID user
   generated in the app; looked up via `getUserControlCode`.
2. **[`Control`](src/tabs/Control.jsx)** — employee confirms the physical ID document matches (photo,
   validity, first/last name, date of birth) and enters the last 6 characters of the document ID;
   submitted via `convertUserControlCode`, which on the backend rejects users that already have
   linked accounts or a mismatched UID.
3. **[`Confirmation`](src/tabs/Confirmation.jsx)** — success screen; the user's identity is now marked
   verified in eduID.

In development these paths are proxied by Vite to `myconext-server` on port 8081
([`vite.config.js`](vite.config.js)). In production/Docker they are proxied by Apache to
`myconextserver:8080`, alongside the OAuth2 login endpoints `/oauth2/authorization` and
`/login/oauth2` (see [`docker/conf/000-default.conf`](docker/conf/000-default.conf)).

## Translations

Unlike `account-gui`/`myconext-gui`, this module's translations are **not** listed in the root
README's [Translations](../README.md#translations) `localicious` commands — they are maintained
directly as plain JS objects in [`src/locale/en.js`](src/locale/en.js) and
[`src/locale/nl.js`](src/locale/nl.js), loaded by [`src/locale/I18n.js`](src/locale/I18n.js).

A Vitest test ([`src/__tests__/locale/en.test.js`](src/__tests__/locale/en.test.js)) enforces that
both bundles declare exactly the same keys, in the same nested order, so `en.js` and `nl.js` can't
drift apart.

## Testing

```shell
yarn test
```

Runs Vitest against [`src/__tests__/`](src/__tests__), which currently covers:

- `locale/en.test.js` — cross-checks that `en.js` and `nl.js` expose identical translation keys
- `stores/AppStore.test.js` — sanity-checks reading/writing the zustand store outside a component
- `utils/Utils.test.js` — `sanitizeURL` and `distinctValues` from
  [`src/utils/Utils.js`](src/utils/Utils.js)

## Build & deployment

Production bundles are produced by Vite (`vite build` → `dist/`) but are normally triggered through
Maven, not invoked directly:

1. `mvn package`/`mvn deploy` at the repo root runs the `exec-maven-plugin` configured in
   [`pom.xml`](pom.xml), which executes [`build.sh`](build.sh):
   ```bash
   rm -Rf public/bundle* target/* build/* dist/*
   nvm use
   yarn install --force && yarn test && yarn build
   ```
   (so a full Maven build also runs the Vitest suite before bundling).
2. The `maven-assembly-plugin` then zips the resulting `dist/` directory per
   [`assembly.xml`](assembly.xml) into the module's build artifact (note: the assembly `<id>` is
   `public-gui`, apparently copied from that module's descriptor — cosmetic, doesn't affect the
   output).
3. For container deployment, [`docker/Dockerfile`](docker/Dockerfile) copies `dist/` into an Apache
   base image (`ghcr.io/openconext/openconext-basecontainers/apache2`), configured by
   [`docker/conf/000-default.conf`](docker/conf/000-default.conf), which serves the SPA (falling back
   to `index.html` for client-side routes) and reverse-proxies `/myconext/api`, `/oauth2/authorization`,
   `/login/oauth2`, `/auth/login`, `/doLogout`, `/internal`, `/robots.txt` and `/config` to
   `myconextserver:8080`.

## Related modules

- [Root README](../README.md) — overall system, System Requirements, running MongoDB/Mailpit, the
  full IdP flow walkthrough
- [`myconext-server`](../myconext-server) — the Spring Boot backend this GUI talks to, in particular
  [`ServiceDeskController`](../myconext-server/src/main/java/myconext/api/ServiceDeskController.java)
- [`account-gui`](../account-gui) — the IdP frontend where the eduID user generates the verification
  code in the first place
- [`myconext-gui`](../myconext-gui) — example Service Provider for local testing
- [`public-gui`](../public-gui) — public content website

---
_Last updated: 2026-07-28_
