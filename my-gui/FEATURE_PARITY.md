# Feature parity: `myconext-gui` (Svelte) vs `my-gui` (React)

Result of a read-only code audit of the old app (`myconext-gui/src`), the new app (`my-gui/src`) and the
server contracts (`myconext-server`: redirects, mail templates, `/config`). Nothing was run against a live
server except where marked. No code was changed for this document.

Legend: **P0** = breaks links/flows that other systems depend on, **P1** = missing user-facing feature,
**P2** = polish / hygiene / verify.

## 0. Implementation status

Decisions: chosen name is always editable (the restriction was an omission); IdP-flow paths on
`accountRedirectUrl` stay with login-gui; the Home banner is replaced by the existing identity card and
alert of the new design; success feedback uses `toast` from `sonner` (curve `Toaster`); changing the
recovery method is confirmed with the eduID app, because the server's `re-*` endpoints require it.

| Gap                          | Status                                         | Notes                                                                                                                                                                                                                                                     |
| ---------------------------- | ---------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| G1 deep links                | Done                                           | `redirectToLogin` keeps `pathname + search`; `/update-email?h=` confirms the change; `/reset-password?h=`, `/add-password?h=`, `/reset-password-link` open the password dialog on `/security` (`PasswordDialog` has `initialHash`, `isPasswordHashValid`) |
| G2 email change              | Done                                           | `EditEmailDialog` (Figma 10565-42233 / 42690), `/edit-email` opens it; verified against mailpit                                                                                                                                                           |
| G3 create from institution   | Done, not run against a real institution login | landing / auto start, `/link/:hash`, `/verify/:hash`, `/expired`, guest error pages, domain checks and flags. No Figma, built from curve components                                                                                                       |
| G4 error pages               | Done                                           | `LinkErrorPage` (curve `Empty`), also for `fromInstitution`; no Figma                                                                                                                                                                                     |
| G5 landing                   | Done                                           | `GuestLayout`, login again / register again                                                                                                                                                                                                               |
| G6 config flags              | Done                                           | `featureIdVerify`, `enableAccountLinking` (nav, home card, institutions buttons), `Config` type, `expirationNonValidatedDurationDays`                                                                                                                     |
| G7 chosen name               | Done                                           | always editable                                                                                                                                                                                                                                           |
| G8 `?retry` / `?servicedesk` | Done                                           | `/identity` opens the verify dialog on the iDIN options or the service desk                                                                                                                                                                               |
| G9 institution details       | Partly                                         | expired badge and expiry rule, Studielink delete rule. Not ported: preferred marker, affiliations and identifier lines (not in the design)                                                                                                                |
| G10 change recovery method   | Done                                           | row on `/security`, `ChangeRecoveryDialog` (`SecondFactorDialog`, then `RecoveryDialog change`), `re-*` endpoints, leave-page warning on the code. Not ported: `sessionStorage` copy of the code                                                          |
| G11 feedback                 | Done                                           | toasts for name, email, password delete, passkey, institution, service, access, recovery, app, control code, welcome                                                                                                                                      |
| G12 other                    | Partly                                         | `/install-app` page for non-phones. Not ported: creation date on the account page, missing-translation reporting                                                                                                                                          |
| P2 robustness                | Done                                           | 401 reloads into login, root `ErrorBoundary`, hard-coded strings moved to locale, `<html lang>`, placeholders removed (`/account*` redirect to `/`), `docs/INSTRUCTIONS.md` refreshed                                                                     |
| P2 deployment                | Open                                           | check `react-router.config.ts` `ssr`, SPA fallback, reverse proxy list, `accountBaseUrl` host for `doLogout` in dev                                                                                                                                       |

Not verified in a browser: the guest flow against a real institution, `?verify=` (iDIN / eIDAS) return,
a logged-out deep link through the login, the phone/SMS recovery path.

## 1. Summary

The authenticated shell (Home, Identity, Institutions, Security, Activity), the passkey, password, eduID-app
(enroll, recovery, deactivate, step-up), service-desk, institution link/prefer/delete and
services/tokens flows are implemented in the new app. What is missing is mostly everything the **server
links to from the outside** (mail links and redirects), the **guest "create eduID from institution" flow**,
the **email-change flow**, the **error landing pages**, **change recovery method**, and several
**config flags and edge cases**.

| Area                                              | Status                                             |
| ------------------------------------------------- | -------------------------------------------------- |
| Home / nav / footer / language / ribbon           | Done                                               |
| Name edit, chosen name                            | Done, one behavioural difference (see G7)          |
| Institutions link/delete/prefer                   | Done, details missing (G9)                         |
| Identity verification (iDIN, eIDAS, service desk) | Done, flag + query-param gaps (G6, G8)             |
| Password (add/change/delete via code)             | Done for the in-app flow, **mail links dead** (G1) |
| Passkeys                                          | Done                                               |
| eduID app enroll / deactivate / step-up           | Done; **change recovery method missing** (G10)     |
| Activity (services, tokens, events)               | Done (event log is new, not in old app)            |
| Email change                                      | **Not implemented** (G2)                           |
| `update-email` confirmation                       | **Not implemented** (G1)                           |
| Create eduID from institution (guest)             | **Not implemented** (G3)                           |
| Error landing pages (`eppn-already-linked`, ...)  | **Placeholders** (G4)                              |
| Landing (logout / delete / rate limit)            | Unstyled stub (G5)                                 |
| Feedback after actions (flash/toast)              | **Missing** (G11)                                  |

## 2. Gaps

### P0: server and mail contracts

**G1. Mail links drop their parameters (dead links)**

- `/update-email?h=<hash>[&nav=security]` (`confirmation_update_email` mail): route `_auth.update-email` is a heading. The hash is never used, so `GET /myconext/api/sp/confirm-email?h=` is never called.
- `/reset-password?h=`, `/add-password?h=` (password mails): both just `<Navigate to="/security">` and lose `h`. The old app validates the hash (`GET password-reset-hash-valid?hash=`), shows an "expired, send again" state, then sets the password. The new `PasswordDialog` only supports the in-app code flow and has no API function for `password-reset-hash-valid`.
- `redirectToLogin` (`src/api/index.ts`) sends `window.location.pathname` only, so any deep link with a query string (`/update-email?h=...`) loses it across the login round trip. The server only checks that the redirect path is internal, so `pathname + search` is accepted.

**G2. Email change not implemented**

- `/identity/edit-email` is a placeholder, and the Security email row links to it.
- Old flow: `PUT generate-email-code[?force=true]` (409 duplicate, 406 outstanding password-forgotten then confirm and retry with `force`, other = rate-limit logout), 6-digit code (`PUT verify-email-code`, 403/400 = rate-limit logout), resend (`GET resend-email-code`, throttled by `emailSpamThresholdSeconds`), then navigate to `update-email?h=<hash>&nav=security`. Validation: regex and "differs from current email, case-insensitive".

**G3. Create eduID from institution (guest flow) is a placeholder**

- Routes: `create-from-institution` (+ `?return_to=`), `/link/:hash`, `/verify/:hash`, `/expired`, `/eppn-already-linked`, `/attribute-missing`. The server redirects here (`AccountLinkerController`, lines ~268-284) with `fromInstitution=true`.
- Needs: landing vs auto-start (`createEduIDInstitutionLanding`), gate on `createEduIDInstitutionEnabled`, email + terms form (`POST create-from-institution/email`, 409/412/other), domain checks (`featureWarningEducationalEmailDomain` -> `domain/institutional`, `featureAllowList` -> `domain/allowed`, subdomain matching), code entry (`PUT create-from-institution/verify`, `location` redirect or `/security?new=true`, 403/400 -> expired, resend), `GET create-from-institution/info?hash=`, retry with `forceAuth=true`.
- Must render for **guests** (outside `_auth`). Old app unprotected paths: `/create-from-institution`, `/landing`, `/install-app`.
- Security page must show the welcome message on `?new=true|false`.

**G4. Error landing pages are placeholders** (all redirect to `/identity/<name>` stubs with a heading only)

- `/eppn-already-linked?email=&fromInstitution=`: message + retry (`startLinkAccountFlow`, or create-from-institution with `forceAuth=true` when `fromInstitution`).
- `/attribute-missing[?fromInstitution=]`: same retry.
- `/subject-already-linked`: retry -> `/personal?retry=true`.
- `/external-account-linked-error?error_description=`: show the (sanitised) description, retry -> `/personal?retry=true`.
- Also needs guest variants (`/create-from-institution/eppn-already-linked`, `/attribute-missing`).
- `/expired` has **no route** (the server redirects there in many places), and the old app also lacks it (404). Decide on a real page.

**G5. `/landing` is an unstyled heading**

- Old: three pages (`logout`, `delete`, `ratelimit`) with a "log in again" / "register again" button that calls `redirectToLogin` without a redirect path; no param = 404. New: only `<h1>`, no button.

**G8. Return-flow query params not consumed on `/identity`**

- `?retry=true` (reopen the verify dialog on the iDIN options) and `?servicedesk=...` (open the verify dialog on service desk) are written by the server and the error pages but `Identity` reads neither. `/personal` redirects to `/identity` with the query kept, so these are silently ignored. (`NewLinkedAccount` handles `?institution` and `?verify` correctly.)

### P1: missing features and behaviour

**G6. Config flags**

| Flag                                                                                  | Old                                                         | New                                          |
| ------------------------------------------------------------------------------------- | ----------------------------------------------------------- | -------------------------------------------- |
| `featureIdVerify`                                                                     | gates iDIN/eIDAS options and issuer loading                 | **ignored**                                  |
| `enableAccountLinking`                                                                | gates all institution UI, Home banner, verification banners | only the Identity alert                      |
| `featureOidcTokenAPI`                                                                 | loads tokens                                                | honoured, but missing from the `Config` type |
| `expirationNonValidatedDurationDays`                                                  | expiry for linked accounts without names (`markExpired`)    | not used; expiry is only `expiresAt`         |
| `createEduIDInstitution*`, `featureWarningEducationalEmailDomain`, `featureAllowList` | guest flow                                                  | not used (G3)                                |

**G7. Name editing difference**

- Old: chosen name is always editable (inline), given/family name only when no valid preferred account.
- New: chosen name only editable when verified. Decide which is intended and align (this may be a deliberate design decision).

**G9. Institution details**

- Old shows: expired marker (non-validated accounts expire after `expirationNonValidatedDurationDays`), preferred marker, affiliations, `subjectId`/eppn, Studielink variant, and hides delete for `idpScoping === "studielink"` unless `connectionActive === false`.
- New `InstitutionCard`: names, received on, valid until, delete. Missing: expired state ("Verify now" expired banner variant), preferred marker, Studielink rule.

**G10. Change recovery method not implemented**

- Old: `/backup-codes` (shows "backup code" or "SMS" + masked phone `** ** *** nnnn`), `/use-app` step-up, then `/change-recovery`, `change-phone-verification` (`re-send-phone-code`), `change-phone-confirmation` (`re-verify-phone-code`), `change-recovery-code` (`re-generate-backup-code`), `change-congrats`.
- New: no recovery row on Security, no change flow, no `re-*` endpoints in `src/api/tiqr.ts`.
- Related: the recovery code is shown once. Old guards the page (`beforeunload`, back-button warning, `sessionStorage["recovery-code"]` fallback, "registration already finalized" screen). The new dialog only blocks dismissal.

**G11. No feedback after actions**

- Old shows a flash for: name updated, email confirmed, institution removed/preferred, passkey renamed/deleted/test result, password changed/deleted, service/eduID deleted, token revoked, "code sent to <email>", welcome. New has none (`curve-react` provides `Sonner`/`Toaster`, not mounted). The passkey test result is inline.

**G12. Other**

- `install-app` is a guest page in the old app (non-mobile -> `/get-app` page). New redirects non-mobile to `/security`, which requires login.
- Home banner "connect your institution / get the app" when there are no linked accounts (old) vs a dismissible intro alert (new). Confirm whether intentional.
- Account page showed the creation date; the new user menu does not.
- Missing translation reporting (`POST /sp/error`) is not ported (optional).

### P2: robustness and hygiene

- **Session handling**: a 401 shows the generic error instead of redirecting to login (302/opaque redirect already reloads, as in the old app).
- **Errors/404**: no `ErrorBoundary`. `_auth.$` makes unknown paths hit login first for guests. A failed `config`/`me` query shows a bare line.
- **Placeholders**: `/account`, `/account/delete`, `/identity/*` stubs are dead ends. `docs/INSTRUCTIONS.md` is stale (route map for `/security/*`, `src/mocks`).
- **Hard-coded strings**: `https://www.idin.nl/` and the trailing `.` in `VerifyIdentityDialog`, `aria-label` in `StoreBadges`, `<html lang="en">` in `root.tsx`.
- **Rate-limit logout host**: dialogs use `accountBaseUrl` (`localhost:3000` in dev, not proxied). Check dev vs prod behaviour.
- **Vite proxy** only forwards `/config`, `/tiqr`, `/register`, `/doLogout`, `/myconext/api`. Confirm that `/oidc`, `/mobile`, `/oauth2`, `/create-from-institution-login` are routed by the production reverse proxy, and that `/client/mobile/*` does not reach `my-gui`.
- **Production routing**: confirm `react-router.config.ts` `ssr` setting and SPA fallback so `/landing`, `/personal`, `/update-email` work on a hard load.
- **Config types**: add `featureOidcTokenAPI`, `createEduIDInstitution*`, `featureAllowList`, `featureWarningEducationalEmailDomain`, `expirationNonValidatedDurationDays` to `Config`.

### Deliberately dropped in the old app (do not port)

- `Services.svelte` and the `/services`, `/institution` routes (no tab, would crash `Home`), `Public.svelte`, legacy `/edit-name` (not linked).
- Old bugs: `ServiceDesk` prefilling first name from last name, `Footer` raw flash key, `Credential` crash on unknown id, broken `addLeadingZero`, whole-user PUT on name edit.

### New in `my-gui` (not in the old app)

- Activity page with the event log (`/sp/activities`) and "Show more", Home recent activity, badge counts, dark mode, user menu, mobile navigation sheet, `NewLinkedAccount` choice dialog with radio selection.

## 3. Implementation plan

Order is by risk: first anything other systems link to, then the missing flows, then polish. Each step lists
the reuse points. Follow the standing rules: curve-react only, no hard-coded strings (en/nl/pt), at most one
`useEffect` per component, `yarn check` before finishing, nothing committed without being asked.

### Phase 1: Server contracts (P0)

1. **Deep links through login** (`src/api/index.ts`, `src/routes/_auth.tsx`): pass `pathname + search` to `redirectToLogin`. Verify with `/update-email?h=x` while logged out.
2. **`/update-email`** (`routes/_auth.update-email`): new `confirmEmail(hash)` in `src/api` (`GET confirm-email?h=`), update the `['me']` cache, toast, navigate to `/security` when `nav=security` else `/identity`; error -> not-found page. Remove the heading.
3. **Password mail links** (`routes/reset-password`, `routes/add-password`, `reset-password-link`): add `isPasswordHashValid(hash)` to `src/api/password.ts`. Extend `PasswordDialog` with an entry that starts at the `password` step with a given hash (it already has the `password`/`expired` steps, `updatePassword`, `setLoginPreference`, "send again"). Routes render `/security` with the dialog open (`?h=` handled in `_auth.security` like `?credential=`). `reset-password-link` opens the dialog at `confirm`.
4. **Error pages** (`_auth.identity.*` + guest variants): one shared `LinkError` dialog/page component (Alert + retry button) with variants `eppn-already-linked`, `attribute-missing`, `subject-already-linked`, `external-account-linked-error` (sanitise `error_description`, add `dompurify` or render as text). Retry uses `startLinkAccountFlow` / `/identity?retry=true`. Needs Figma or a minimal curve `Alert`/`Empty` layout (ask the user for designs if in doubt).
5. **`/identity` query params**: read `retry` and `servicedesk` in `routes/_auth.identity/route.tsx` and open `VerifyIdentityDialog` with `initialView` (`'banks'` / `'serviceDesk'`); clear the params on close (same pattern as `NewLinkedAccount`).
6. **`/landing`**: style with curve (title, text, "log in again"/"register again" button via `redirectToLogin`), 404 without params, public route.
7. **`/expired`**: decide target (public page with "start again").

### Phase 2: Missing flows

8. **Email change** (replace `_auth.identity.edit-email`): `EditEmailDialog` reusing the `PasswordDialog` pattern (confirm -> code via `InputOTP` -> resend throttling via `emailSpamThresholdSeconds` -> redirect to `update-email?h=`). New API functions `generateEmailCode(email, force)`, `verifyEmailCode`, `resendEmailCode`. Handle 409, 406 (outstanding password link confirm + `force`), 403/400 rate-limit logout (reuse the logout call in `PasswordDialog`). Open from the Security email row (`LoginMethodRow onChange`).
9. **Create eduID from institution** (guest): new public routes under `routes/create-from-institution*`, API module `src/api/createFromInstitution.ts`, pages per the old flow (landing, email + terms + domain checks, code verify, expired, errors with `fromInstitution`). Gate on `createEduIDInstitutionEnabled`; add the Security `?new=` welcome toast. Needs Figma (none yet): ask the user before building.
10. **Change recovery method**: add a recovery row on Security (backup code / masked phone) and `ChangeRecoveryDialog` reusing `RecoveryDialog` steps and `SecondFactorDialog` as the first step. Add `re-send-phone-code`, `re-verify-phone-code`, `re-generate-backup-code` to `src/api/tiqr.ts`. Port the recovery-code guard (`beforeunload`, `sessionStorage` fallback, finalized screen). Needs Figma for the row and flow.
11. **Institutions details** (`InstitutionCard`): expired state + banner, preferred marker, affiliations/identifier, Studielink delete rule (`utils/verification.ts`, `locale`). Needs the old expiry logic (`markExpired`) ported into `utils/verification.ts`.

### Phase 3: Behaviour and polish

12. **Flags**: honour `featureIdVerify` (hide iDIN/eIDAS in `VerifyIdentityDialog`), `enableAccountLinking` on Institutions/Home, type the new `Config` fields, use `expirationNonValidatedDurationDays`.
13. **Feedback**: mount curve `Toaster` in `root.tsx`, add toasts after name/email/password/passkey/institution/service/token changes and the passkey test result.
14. **Name editing rule** (G7): align after the user confirms the intended behaviour.
15. **Robustness**: 401 handling in `src/api/request.ts` (redirect to login), root-level `ErrorBoundary` and a public 404, `install-app` guest page for non-mobile.
16. **Hygiene**: remove or implement `/account*` placeholders, fix hard-coded strings, refresh `docs/INSTRUCTIONS.md`.
17. **Deployment checks**: `react-router.config.ts` `ssr`, SPA fallback, reverse proxy list, `accountBaseUrl` vs dev host for `doLogout`.

### Open questions for the user

1. Is the chosen-name restriction for unverified users intended (G7)?
2. Are there Figma designs for: email change, create-from-institution (guest), error pages, landing, change recovery method, recovery row on Security?
3. Should `my-gui` serve the IdP-flow paths on `accountRedirectUrl` (`/subject-already-linked/{id}`, `/affiliation-missing/`, ...) or are they login-gui's job? (Assumed login-gui.)
4. Keep the Home intro alert, or restore the old "connect your institution" banner when nothing is linked?

## 4. Verification plan (once implemented)

- `yarn check` in `my-gui`.
- For each P0 item, open the exact server/mail URL logged out and logged in: `/update-email?h=...`, `/reset-password?h=...`, `/add-password?h=...`, `/personal?institution=...`, `/personal?verify=...`, `/personal?retry=true`, `/personal?servicedesk=start`, `/credential?id=...&success=true`, `/landing?logout|delete|ratelimit=true`, `/eppn-already-linked`, `/attribute-missing`, `/subject-already-linked`, `/external-account-linked-error?error_description=x`, `/create-from-institution/...`.
- Mail links via mailpit (`http://localhost:8025`), SMS via the dev mock file, tiqr via `http://localhost:8088`.
- Toggle each config flag in the server `application.yml` and check the UI.
- Compare against the old app (`http://localhost:3002`) side by side per flow.

## 5. Caveats of this audit

- Based on reading code only (three parallel read-only audits). Items marked "verify" were not confirmed in a browser.
- The old-app inventory comes from a code read of the Svelte sources, so details such as exact endpoint payloads should be checked against `myconext-gui/src/api/index.js` while implementing.
