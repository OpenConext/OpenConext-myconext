# Public-GUI (Content website)

Public-GUI is the public-facing marketing/content site for **eduID** — the informational website a
visitor lands on at the bare domain (e.g. `eduid.nl`) *before* they have an account. It explains what
eduID is, lets people install the eduID mobile app, hosts the Terms of Use / Privacy Policy, and
serves a couple of Dutch-service-desk-facing pages. It is **not** where anyone logs in or registers —
those actions link out to the other GUIs (see [Overview](#overview)).

## Overview

Public-GUI is one of four frontend modules plus the backend in this repository (see the
[root README](../README.md) for the full picture):

| Module | Role |
|---|---|
| `myconext-server` | Spring Boot backend: SAML/OIDC IdP logic, persistence, mail |
| `account-gui` | The IdP frontend a user authenticates against |
| `myconext-gui` | Example Service Provider used for local development/testing |
| `servicedesk-gui` | Service-desk Service Provider (support staff tooling) |
| **`public-gui`** | **This module** — the public content/marketing website |

Unlike `account-gui` and `servicedesk-gui`, Public-GUI has **no API integration with
`myconext-server`** at runtime: there is not a single `fetch()` call anywhere in `src/` (verified by
grepping the source tree). Instead:

- The "My eduID" and "Create an eduID" buttons ([`Navigation.jsx`](src/components/Navigation.jsx),
  [`Home.jsx`](src/routes/Home.jsx)) link to URLs derived purely from the current hostname —
  [`App.jsx`](src/App.jsx) sets `accountBaseUrl` to `https://login.<host>` and `myconextBaseUrl` to
  `https://mijn.<host>` on mount, assuming this site, `account-gui` and `myconext-gui` are deployed as
  sibling subdomains of the same domain (e.g. `eduid.nl`, `login.eduid.nl`, `mijn.eduid.nl`).
- [`vite.config.js`](vite.config.js) proxies `/config` to `myconext-server` in dev, and
  [`docker/conf/000-default.conf`](docker/conf/000-default.conf) proxies `/config`, `/internal` and
  `/robots.txt` to `myconextserver:8080` in production — but nothing in `src/` currently calls
  `/config`; this looks like boilerplate copied from the sibling GUIs (`account-gui` and
  `servicedesk-gui` both do fetch `/config` at startup) that was never wired up here, or left in place
  for future use.

During local development, `public-gui` runs standalone on port **3002**. In production it is built as
a static bundle and served behind an Apache reverse proxy (see [Build & deployment](#build--deployment)).

## Tech stack

Taken from [`package.json`](package.json). Note: the [root README](../README.md#public-gui-content-website)
correctly identifies this module as built with Vite, but doesn't call out the UI framework — it is
**React 19**, not Svelte (unlike `account-gui`/`myconext-gui`).

- **[React](https://react.dev/) 19.1** + **react-dom 19.1** — component framework (function
  components + hooks; no class components).
- **[Vite](https://vitejs.dev/) ^8** with **[@vitejs/plugin-react-oxc](https://www.npmjs.com/package/@vitejs/plugin-react-oxc)**
  — dev server and bundler. This is the Oxc/Rust-based React plugin, not the Babel-based
  `@vitejs/plugin-react` used by `servicedesk-gui`.
- **[vite-plugin-svgr](https://react-svgr.com/)** — imports `.svg` files as React components.
- **[react-router](https://reactrouter.com/) 7** — client-side routing (see
  [`src/App.jsx`](src/App.jsx) for the route table and [`src/main.jsx`](src/main.jsx) for the
  top-level `BrowserRouter`). Note this project imports directly from `react-router` (not
  `react-router-dom`).
- **[zustand](https://github.com/pmndrs/zustand) 5** — the single app store,
  [`src/stores/AppStore.js`](src/stores/AppStore.js), holding only `config.accountBaseUrl` /
  `config.myconextBaseUrl`.
- **[@surfnet/sds](https://www.npmjs.com/package/@surfnet/sds)** — the SURFconext design-system
  component library (`Button`, `Logo`, `ButtonType`, `LogoType`, ...). Its stylesheets are imported
  first in [`src/main.jsx`](src/main.jsx), with a comment warning not to reorder the imports.
- **[i18n-js](https://github.com/fnando/i18n-js)** — translations, wrapped by
  [`src/locale/I18n.js`](src/locale/I18n.js).
- **[react-qr-code](https://www.npmjs.com/package/react-qr-code)** — renders the QR code on the Home
  and About pages that lets a desktop visitor scan-to-open the [`InstallApp`](src/routes/InstallApp.jsx)
  route on their phone.
- **[react-tooltip](https://react-tooltip.com/)** — declared as a dependency (styles not observed to
  be imported/used elsewhere in the reviewed source).
- **[js-cookie](https://github.com/js-cookie/js-cookie)** — reading/writing the `lang` cookie (see
  [`LanguageSelector.jsx`](src/components/LanguageSelector.jsx)).
- **[isomorphic-dompurify](https://github.com/kkomelin/isomorphic-dompurify)** — sanitizing HTML
  before `dangerouslySetInnerHTML` (e.g. the `home.appInfo` string in [`Home.jsx`](src/routes/Home.jsx),
  which is not actually run through it there — see the Home component for the raw
  `dangerouslySetInnerHTML={{__html: I18n.t(...)}}` usage).
- **sass** — component-scoped `.scss` stylesheets, one per component/route, co-located.
- **[ESLint](https://eslint.org/) 9** flat config ([`eslint.config.js`](eslint.config.js)) with
  `eslint-plugin-react`, `eslint-plugin-react-hooks`, `eslint-plugin-react-refresh`.
- **[Vitest](https://vitest.dev/) 4** — unit tests (not Jest, same as `servicedesk-gui`).

## Project structure

```
public-gui/
├── index.html                # Vite entry HTML (loads /src/main.jsx); sets <title>eduID</title>
├── vite.config.js            # Dev server (port 3002) + /config proxy to myconext-server (8081)
├── eslint.config.js          # Flat ESLint config for React
├── build.sh                  # Invoked by Maven during `mvn package` (see below)
├── assembly.xml               # Maven assembly descriptor, zips up dist/
├── pom.xml                    # Maven module wired into the multi-module build (packaging: pom)
├── docker/
│   ├── Dockerfile             # Apache image serving the built dist/ bundle
│   └── conf/000-default.conf  # Apache rewrite rules + ProxyPass to myconextserver:8080
├── public/
│   ├── manifest.webmanifest   # PWA manifest (eduID branding, links to Play Store)
│   ├── favicon.ico, alive.txt, .htaccess
│   └── .well-known/           # apple-app-site-association, assetlinks.json (mobile app deep links
│                               # for /tiqrauth, /tiqrenroll, /client/mobile/*), plus a `test` file
└── src/
    ├── main.jsx               # App bootstrap: mounts <App/> inside a BrowserRouter, loads SDS styles
    ├── App.jsx                # Top-level route table; derives accountBaseUrl/myconextBaseUrl from host
    ├── routes/                # One component per page/route — see Available routes below. Several
    │                          # pages (About, Support, ServiceDesk, Verify) are thin locale switches
    │                          # that render an *_EN.jsx or *_NL.jsx sibling component
    ├── components/             # Header, Footer, Navigation, MobileNavigation, LanguageSelector,
    │                          # Background, CollapseField, InfoLinkField, AnchorLink
    ├── terms/                  # PrivacyEN/NL.jsx, TermsEN/NL.jsx — the long-form legal text pages
    ├── stores/AppStore.js       # zustand store: config.accountBaseUrl / config.myconextBaseUrl
    ├── locale/                  # I18n.js wrapper + en.js/nl.js translation bundles (50 lines each)
    ├── hooks/useFragmentOpen.js # Tracks which FAQ/collapse section is open via the URL fragment
    ├── utils/                   # Utils.js (isEmpty, stopEvent), QueryParameters.js
    ├── assets/                  # SVG/PNG images (logo, app-store badges, illustrations, EU logo)
    └── __tests__/               # Vitest unit test(s) — see Testing
```

## Available routes

From the `<Routes>` table in [`src/App.jsx`](src/App.jsx):

| Path | Component | Purpose |
|---|---|---|
| `/` | redirects to `/home` | |
| `/home` | [`Home`](src/routes/Home.jsx) | Landing page: what eduID is, app download QR code / store badges |
| `/about` | [`About`](src/routes/About.jsx) → `About_EN`/`About_NL` | Longer explanation of eduID |
| `/support` | [`Support`](src/routes/Support.jsx) → `Support_EN`/`Support_NL` | Help/FAQ content |
| `/verify` | [`Verify`](src/routes/Verify.jsx) → `Verify_EN`/`Verify_NL` | Explains identity verification |
| `/servicedesk`, `/servicedesk-en` | [`ServiceDesk`](src/routes/ServiceDesk.jsx) → `ServiceDesk_NL`/`ServiceDesk_EN` | Content aimed at service-desk visitors; the path itself pins the locale (`/servicedesk` = Dutch, `/servicedesk-en` = English) — see [Translations](#translations) |
| `/terms` | [`Terms`](src/routes/Terms.jsx) → `terms/TermsEN`/`TermsNL` | Terms of Use |
| `/privacy` | [`Privacy`](src/routes/Privacy.jsx) → `terms/PrivacyEN`/`PrivacyNL` | Privacy Policy |
| `/install-app` | [`InstallApp`](src/routes/InstallApp.jsx) | Detects iOS/Android from the user agent and redirects to the app/Play/App Store URL, or falls back to `/home` on desktop |
| `/tiqrauth/*` | [`TiqrAuth`](src/routes/TiqrAuth.jsx) | Reads `u`, `s`, `q`, `i`, `v` query params and redirects to the `eduidauth://` custom URL scheme to hand off to the mobile app |
| `/nav` | [`MobileNavigation`](src/routes/MobileNavigation.jsx) | Full-screen mobile menu (opened via the hamburger icon in `Header`) |
| `*` | [`NotFound`](src/routes/NotFound.jsx) | 404 page |

## Prerequisites

- Node.js `24.3.0` — pinned in [`.nvmrc`](.nvmrc); run `nvm use` before installing.
- Yarn (Yarn 1.x per the [root README](../README.md)).
- `myconext-server` is **not** required to browse this site locally — every route renders from static
  translation strings and client-side redirects. It's only relevant if you want the "My eduID" /
  "Create an eduID" links to resolve to a real, running IdP.

## Getting started

```shell
cd public-gui
nvm use
yarn install
yarn dev
```

The dev server starts on **http://localhost:3002** and opens automatically, redirecting to `/home`.

## Available scripts

From [`package.json`](package.json):

| Script | Command | Description |
|---|---|---|
| `yarn dev` | `vite` | Start the Vite dev server on port 3002 |
| `yarn build` | `vite build` | Produce a production bundle in `dist/` |
| `yarn lint` | `eslint .` | Lint the source tree using [`eslint.config.js`](eslint.config.js) |
| `yarn preview` | `vite preview` | Serve the built `dist/` bundle locally |
| `yarn test` | `vitest` | Run the Vitest suite under `src/__tests__/` |

## Configuration

There is no `.env`/`.env.example` file, and — unlike `account-gui`/`servicedesk-gui` — this app does
not fetch runtime configuration from the backend. Instead:

- [`App.jsx`](src/App.jsx) derives `accountBaseUrl`/`myconextBaseUrl` synchronously from
  `window.location.host` on mount (see [Overview](#overview)), so switching environments only requires
  deploying this site under the right hostname — no build-time or server-side config is involved.
- Locale is resolved in [`src/locale/I18n.js`](src/locale/I18n.js) from the `lang` query parameter,
  then the `lang` cookie, then the browser language, defaulting to English — except the `/servicedesk`
  and `/servicedesk-en` paths, which hard-code the locale to `nl`/`en` respectively regardless of the
  above.
- The `/config` proxy target in [`vite.config.js`](vite.config.js) (`http://localhost:8081`) exists
  but, per the [Overview](#overview) note, is not currently exercised by any code in `src/`.

## Translations

Like `servicedesk-gui` and unlike `account-gui`/`myconext-gui`, this module's translations are **not**
part of the root README's [Translations](../README.md#translations) `localicious` commands — they are
maintained directly as plain JS objects in [`src/locale/en.js`](src/locale/en.js) and
[`src/locale/nl.js`](src/locale/nl.js) (50 lines each), loaded by
[`src/locale/I18n.js`](src/locale/I18n.js). Missing keys render as `[missing "<scope>" translation]`
rather than throwing.

A Vitest test ([`src/__tests__/locale/en.test.js`](src/__tests__/locale/en.test.js)) enforces that
`en.js` and `nl.js` declare exactly the same keys, in the same nested order, so the two bundles can't
drift apart. Long-form content — the About/Support/ServiceDesk/Verify page bodies and the Terms/Privacy
legal text — is not in these locale files at all; it's hard-coded directly as JSX/English or Dutch text
inside the respective `*_EN.jsx`/`*_NL.jsx` components (e.g. [`About_EN.jsx`](src/routes/About_EN.jsx),
[`terms/TermsNL.jsx`](src/terms/TermsNL.jsx)), which is why those routes are structured as a locale
switch at the route level rather than using `I18n.t()` throughout.

## Testing

```shell
yarn test
```

Runs Vitest against [`src/__tests__/`](src/__tests__), which currently covers only:

- `locale/en.test.js` — cross-checks that `en.js` and `nl.js` expose identical translation keys

Unlike `account-gui`/`servicedesk-gui`, there are no store or utility tests here yet.

## Build & deployment

Production bundles are produced by Vite (`vite build` → `dist/`) but are normally triggered through
Maven, not invoked directly:

1. `mvn package`/`mvn deploy` at the repo root runs the `exec-maven-plugin` configured in
   [`pom.xml`](pom.xml), which executes [`build.sh`](build.sh):
   ```bash
   rm -Rf target/*
   rm -Rf dist/*
   nvm use
   export CI=true
   yarn install --force && yarn test && yarn lint && yarn build
   ```
   (so a full Maven build also runs the Vitest suite and ESLint before bundling — this module is the
   only one of the four GUIs whose `build.sh` runs `yarn lint` as part of the build.)
2. The `maven-assembly-plugin` then zips the resulting `dist/` directory per
   [`assembly.xml`](assembly.xml) (`<id>public-gui</id>`) into the module's build artifact.
3. For container deployment, [`docker/Dockerfile`](docker/Dockerfile) copies `dist/` into an Apache
   base image (`ghcr.io/openconext/openconext-basecontainers/apache2`), configured by
   [`docker/conf/000-default.conf`](docker/conf/000-default.conf), which serves the SPA (falling back
   to `index.html` for client-side routes, with exceptions for static assets, `manifest.json`,
   `/config`, `/internal`, `/robots.txt`, `/fonts` and `/.well-known`) and reverse-proxies
   `/robots.txt`, `/internal` and `/config` to `myconextserver:8080`.

Note: [`public/.htaccess`](public/.htaccess) contains additional standalone Apache rewrite rules
(redirecting `security.txt` to `surf.nl`, and `validated/*`/`trust/*` to SURF wiki documentation pages)
that don't appear in `docker/conf/000-default.conf` — it's unclear whether `.htaccess` is actually
picked up by the Docker/Apache deployment or is a leftover from a different (non-container) hosting
setup.

## Related modules

- [Root README](../README.md) — overall system, System Requirements, running MongoDB/Mailpit, the
  full IdP flow walkthrough
- [`myconext-server`](../myconext-server) — the Spring Boot backend `account-gui`/`servicedesk-gui`
  talk to (this module doesn't call it directly at runtime — see [Overview](#overview))
- [`account-gui`](../account-gui) — the IdP frontend that `myconextBaseUrl`/`accountBaseUrl` links
  point to for the actual login/registration flow
- [`myconext-gui`](../myconext-gui) — example Service Provider for local testing
- [`servicedesk-gui`](../servicedesk-gui) — service-desk Service Provider

---
_Last updated: 2026-07-28_
